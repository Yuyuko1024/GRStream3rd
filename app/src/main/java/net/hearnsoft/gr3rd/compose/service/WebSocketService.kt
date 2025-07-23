package net.hearnsoft.gr3rd.compose.service

import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.widget.Toast
import com.blankj.utilcode.util.SPStaticUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.Strictness
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.hearnsoft.gr3rd.compose.R
import net.hearnsoft.gr3rd.compose.domain.beans.NowPlayingData
import net.hearnsoft.gr3rd.compose.domain.beans.RadioClientData
import net.hearnsoft.gr3rd.compose.domain.viewmodel.SongViewModel
import net.hearnsoft.gr3rd.compose.socket.GRWebSocketClient
import net.hearnsoft.gr3rd.compose.utils.Constants
import net.hearnsoft.gr3rd.compose.utils.Logger
import net.hearnsoft.gr3rd.compose.utils.NullStringToEmptyAdapterFactory
import java.net.URI
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import kotlin.random.Random

class WebSocketService : Service() {
    companion object {
        const val TAG = "WebSocketService"
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 使用共享的 ViewModel 实例
    private val songViewModel by lazy { SongViewModel.getInstance() }

    private lateinit var uri: URI
    private lateinit var gson: Gson
    private lateinit var connMgr: ConnectivityManager
    private lateinit var webSocketClient: GRWebSocketClient
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    private var isRunning = false
    private var recheck = 0
    private var clientId: Int = 0

    private val pong: JsonObject = JsonObject().apply {
        addProperty("message", "pong")
        addProperty("id", clientId)
    }

    override fun onCreate() {
        super.onCreate()
        // 在 onCreate 中初始化
        uri = URI.create(Constants.WS_URL)
        gson = GsonBuilder()
            .disableHtmlEscaping()
            .setStrictness(Strictness.LENIENT)
            .serializeNulls()
            .setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .registerTypeAdapterFactory(NullStringToEmptyAdapterFactory())
            .create()

        // 初始化 WebSocket 客户端
        initWebSocketClient()

        // 初始化网络监听
        connMgr = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        setupNetworkCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 只启动一次
        if (!isRunning) {
            isRunning = true
            serviceScope.launch {
                initWSConn()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?) = null

    // 设置网络回调
    private fun setupNetworkCallback() {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Logger.info(TAG, "Network is available")

                // 更新ViewModel网络状态、
                songViewModel.updateNetworkStatus(true)

                if (!webSocketClient.isOpen) {
                    Logger.info(TAG, "Network available, attempting to reconnect WebSocket...")
                    serviceScope.launch {
                        initWebSocketClient()
                        initWSConn()
                    }
                }

                serviceScope.launch {
                    showNetworkStatusToast(true)
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Logger.warn(TAG, "Network is lost")

                // 更新 ViewModel 中的网络状态
                songViewModel.updateNetworkStatus(false)
                songViewModel.updateWebSocketStatus(false)

                closeWsClient()

                serviceScope.launch {
                    showNetworkStatusToast(false)
                }
            }
        }
        // 注册网络回调
        connMgr.registerDefaultNetworkCallback(networkCallback)
    }

    // 初始化 WebSocket 客户端
    private fun initWebSocketClient() {
        webSocketClient = GRWebSocketClient(
            serverUri = uri,
            onMessage = {
                serviceScope.launch {
                    extractData(it)
                }
            },
            onOpen = {
                // 连接成功时更新状态
                Logger.info(TAG, "WebSocket connected successfully")
                songViewModel.updateWebSocketStatus(true)
                recheck = 0 // 重置重连计数
            },
            onClose = { code, reason, remote ->
                Logger.info(TAG, "WebSocket connection closed: code=${code}, reason=${reason}, remote=${remote}")
                songViewModel.updateWebSocketStatus(false)
                // 如果是正常关闭（code 1000），则不需要重连
                if (code != 1000 || !remote) {
                    // 处理非正常关闭
                    Logger.warn(TAG, "WebSocket closed unexpectedly: code=$code, reason=$reason")
                    Logger.info(TAG, "Attempting to reconnect WebSocket...")
                    // 重新连接 WebSocket
                    serviceScope.launch {
                        reconnectWebSocket()
                    }
                }
            },
            onError = {
                Logger.err(TAG, "WebSocket error occurred", it)
                songViewModel.updateWebSocketStatus(false)
            }
        )
        webSocketClient.setSocketFactory(getDefaultSSLSocketFactory())
    }

    // 初始化 WebSocket 连接
    private fun initWSConn() {
        try {
            webSocketClient.connectBlocking()
            // 连接成功后发送欢迎消息并更新状态
            if (webSocketClient.isOpen) {
                // 连接成功后，发送欢迎消息
                Logger.info(TAG, "WebSocket client initialized and connected")
                songViewModel.updateWebSocketStatus(true)
                webSocketClient.send(Constants.WS_SESSION_MSG.toString())
            }
        } catch (e: InterruptedException) {
            // 处理异常
            e.printStackTrace()
        } catch (e: Exception) {
            // 处理其他异常
            e.printStackTrace()
        }
    }

    // 处理接收到的数据
    private suspend fun extractData(data: String) {
        if (data.isEmpty()) {
            Logger.warn(TAG, "Received empty data")
            return
        }

        data.let {data ->
            Logger.debug(TAG, "Received data: $data")

            when {
                isJson(data) -> handleJsonData(data)
                data.startsWith("Error") -> showErrorToast(data)
                else -> Logger.warn(TAG, "Unknown data format: $data")
            }
        }
    }

    // 处理 JSON 数据
    private fun handleJsonData(data: String) {
        when {
            data.contains("welcome") -> {
                val clientData = gson.fromJson(data, RadioClientData::class.java)
                clientId = clientData.id
                Logger.info(TAG, "WebSocket connected with client ID: ${clientData.id}")
                SPStaticUtils.put(Constants.PREF_CLIENT_ID, clientId)
            }
            data == Constants.WS_PING_MSG.toString() -> {
                Logger.debug(TAG, "Received ping from server, sending pong")
                sendPong()
            }
            else -> genBeanData(data)
        }
    }

    // 处理服务器发来的专辑信息
    private fun genBeanData(string: String) {
        try {
            val nowPlayingData = gson.fromJson(string, NowPlayingData::class.java)
            // 更新 ViewModel 中的当前播放数据
            songViewModel.updateFromWebSocket(nowPlayingData)
            Logger.info(TAG, "Updated now playing data: ${nowPlayingData.title}")
        } catch (e: Exception) {
            Logger.err(TAG, "Error parsing now playing data", e)
        }
    }

    // 关闭 WebSocket 客户端
    private fun closeWsClient() {
        try {
            if (webSocketClient.isOpen) {
                webSocketClient.close()
            }
        } catch (e: Exception) {
            Logger.err(TAG, "Error closing WebSocket client", e)
        } finally {
            webSocketClient.close()
            isRunning = false
            recheck = 0
        }
    }

    // 重新连接 WebSocket
    private suspend fun reconnectWebSocket() {
        recheck++
        postSocketErrorToast(recheck)

        val delayMillis = calculateReconnectDelay(recheck)
        Logger.info(TAG, "Will reconnect in ${delayMillis}ms...")

        delay(delayMillis)

        try {
            initWebSocketClient()
            initWSConn()
        } catch (e: Exception) {
            Logger.err(TAG, "Reconnection failed", e)
        }
    }

    // 发送心跳包
    private fun sendPong() {
        if (webSocketClient.isOpen) {
            webSocketClient.send(pong.toString())
            Logger.debug(TAG, "Sent ping to WebSocket server")
        } else {
            Logger.warn(TAG, "WebSocket is not open, cannot send ping")
        }
    }

    // 计算重连延时
    private fun calculateReconnectDelay(retryCount: Int): Long {
        val baseDelay = when (retryCount) {
            1 -> 1_000L to 10_000L
            2 -> 10_000L to 20_000L
            3 -> 15_000L to 30_000L
            4 -> 30_000L to 45_000L
            else -> 45_000L to 65_000L
        }
        return Random.nextLong(baseDelay.first, baseDelay.second)
    }

    private fun getDefaultSSLSocketFactory() : SSLSocketFactory {
        return try {
            val sslContext = SSLContext.getDefault()
            sslContext.init(null, null, null)
            sslContext.socketFactory
        } catch (e: Exception) {
            Logger.err(TAG, "Error creating SSLContext", e)
            SSLContext.getDefault().socketFactory
        }
    }

    private suspend fun postSocketErrorToast(recheckCount: Int) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                applicationContext,
                getString(R.string.socket_error_reconnect) + recheckCount,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private suspend fun showErrorToast(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                applicationContext,
                getString(R.string.socket_error_server_msg) + message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isJson(data: String) : Boolean {
        val jsonElement = JsonParser.parseString(data);
        return jsonElement.isJsonObject;
    }

    private suspend fun showNetworkStatusToast(isAvailable: Boolean) {
        val message = if (isAvailable) {
            getString(R.string.network_restored)
        } else {
            getString(R.string.network_lost)
        }
        withContext(Dispatchers.Main) {
            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeWsClient()
        isRunning = false
    }
}