package net.hearnsoft.gensokyoradio.trd.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.hearnsoft.gensokyoradio.trd.R;
import net.hearnsoft.gensokyoradio.trd.beans.NowPlayingBean;
import net.hearnsoft.gensokyoradio.trd.beans.SocketClientBeans;
import net.hearnsoft.gensokyoradio.trd.utils.Constants;
import net.hearnsoft.gensokyoradio.trd.utils.NullStringToEmptyAdapterFactory;
import net.hearnsoft.gensokyoradio.trd.ws.GRWebSocketClient;

import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class WebSocketService extends Service {
    private static final String TAG = WebSocketService.class.getSimpleName();
    private static WsServiceInterface wsInterface;
    private final ExecutorService signalThreadPool = Executors.newSingleThreadExecutor();
    private GRWebSocketClient wsClient;
    private SharedPreferences.Editor spEditor;
    private MediaMetadata.Builder metadata;
    private MediaSession mMediaSession;
    private Gson gson;
    private Bitmap cover;
    private NotificationManager notiMgr;
    private Notification.Builder notiBuilder;
    private Notification notification;
    private MediaSessionCallback callback;
    private URI uri;
    private int clientId;

    public static void setCallback(WsServiceInterface dataInterface) {
        wsInterface = dataInterface;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        uri = URI.create(Constants.WS_URL);
        SharedPreferences sharedPreferences = getSharedPreferences("ws", MODE_PRIVATE);
        spEditor = sharedPreferences.edit();
        notiMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        metadata = new MediaMetadata.Builder();
        mMediaSession = new MediaSession(this, "MyPlayer");
        NotificationChannel channel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_ID,  NotificationManager.IMPORTANCE_DEFAULT);
        notiMgr.createNotificationChannel(channel);
        callback = new MediaSessionCallback();
        mMediaSession.setActive(true);
        mMediaSession.setCallback(callback);
        gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setLenient()
                .serializeNulls()
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .create();
        initWebSocket();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        signalThreadPool.submit(this::initConn);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // Service unbind, close WebSocket client
        // Service 解绑，关闭 WebSocket 客户端
        closeWsClient();
        return false;
    }

    private void initWebSocket() {
        wsClient = new GRWebSocketClient(uri) {
            @Override
            public void onMessage(String message) {
                extractData(message);
            }
        };
        wsClient.setSocketFactory(getDefaultSSLSocketFactory());
    }

    /**
     * Initiate received data processing
     * 初始化接收到的数据处理
     * @param message
     */
    private void extractData(String message) {
        if (message != null) {
            Handler toastHandler = new Handler(Looper.getMainLooper());
            Log.d(TAG, "socket message:" + message);
            if (isJson(message)) {
                if (message.contains("welcome")) {
                    SocketClientBeans clientBeans = gson.fromJson(message, SocketClientBeans.class);
                    clientId = clientBeans.id;
                    Log.d(TAG, "get Client ID: " + clientId);
                    spEditor.putInt("clientId", clientId);
                    spEditor.apply();
                } else if (message.equals("{\"message\":\"ping\"}")) {
                    Log.d(TAG, "get ping! send pong!");
                    sendPong();
                } else {
                    // Generate bean data
                    genBeanData(message);
                }
            } else if (message.startsWith("Error")) {
                toastHandler.post(() -> Toast.makeText(getApplicationContext(),"ERROR: \n Received an error message from server: \n" + message, Toast.LENGTH_SHORT).show());
            } else {
                Log.e(TAG, "get invalid json data!");
                toastHandler.post(() -> Toast.makeText(getApplicationContext(),"ERROR: received error json data !", Toast.LENGTH_SHORT).show());
            }
            /*if (message.startsWith("welcome")) {
                clientId = Integer.parseInt(message.split("welcome:")[1]);
                Log.d(TAG, "get Client ID: " + clientId);
                spEditor.putInt("clientId", clientId);
                spEditor.apply();
            } else if (message.equals("{\"message\":\"ping\"}")) {
                Log.d(TAG, "get ping! send pong!");
                sendPong();
            } else if (message.startsWith("Error")) {

                toastHandler.post(() -> Toast.makeText(getApplicationContext(),"ERROR: \n Received an error message from server: \n" + message, Toast.LENGTH_SHORT).show());
            } else {
                // Generate bean data
                if (isJson(message)) {
                    genBeanData(message);
                } else {
                    Log.e(TAG, "get invalid json data!");
                }
            }*/
        } else {
            Log.e(TAG, "get null data!");
        }
    }

    /**
     * Get default SSLSocketFactory
     * 获取默认 SSLSocketFactory
     */
    private SSLSocketFactory getDefaultSSLSocketFactory() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getDefault();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslContext.getSocketFactory();
    }

    /**
     * Send initial connection message
     * 发送初始连接消息
     * @params null
     */
    private void initConn() {
        // Connect to WebSocket server and waiting for welcome message
        // 连接到 WebSocket 服务器并等待 welcome 消息
        try {
            wsClient.connectBlocking();
        } catch (InterruptedException e) {
            Log.e(TAG, "webSocket connect time out!");
            e.printStackTrace();
        }
        // If the connection is successful, send the initial connection message
        // 如果连接成功，发送初始连接消息
        if (wsClient != null && wsClient.isOpen()) {
            wsClient.send(Constants.WS_SESSION_MSG);
        }
    }

    /**
     * Generate bean data
     * 从 json 反序列化生成 bean 数据
     * @param jsonData
     */
    private void genBeanData(String jsonData) {
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setLenient()
                .serializeNulls()
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .registerTypeAdapterFactory(new NullStringToEmptyAdapterFactory<String>())
                .create();
        NowPlayingBean bean = gson.fromJson(jsonData, NowPlayingBean.class);
        wsInterface.beanReceived(bean);

        genMediaNotification(bean);
    }

    /**
     * Check data is JSON data
     * 检查数据是否为 JSON 数据
     * @param jsonData
     * @return boolean isJson
     */
    private boolean isJson(String jsonData) {
        JsonElement jsonElement;
        try {
            jsonElement = JsonParser.parseString(jsonData);
            return jsonElement.isJsonObject();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Send pong message
     * 发送 pong 心跳包消息
     * @params null
     */
    private void sendPong() {
        wsClient.send("{\"message\":\"pong\", \"id\":" + clientId + "}");
    }

    private void genMediaNotification(NowPlayingBean bean) {
        metadata = new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, bean.getTitle())
                .putString(MediaMetadata.METADATA_KEY_ARTIST, bean.getArtist())
                .putLong(MediaMetadata.METADATA_KEY_DURATION, bean.getDuration());
        mMediaSession.setMetadata(metadata.build());
        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        if (downloadImage(bean.getAlbumArt())) {
            buildNotification(mMediaSession.getSessionToken(), bean.getTitle(), bean.getArtist(), cover);
        }
    }

    private boolean downloadImage(String url) {
        boolean success = false;
        Bitmap bitmap = null;
        try {
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bitmap != null) {
                // 将Bitmap数据存入变量中
                cover = bitmap;
            }
        }
        return success;
    }

    /**
     * Build Media Notification
     * @param token
     * @param name
     * @param artist
     * @param cover
     */
    private void buildNotification(MediaSession.Token token,String name,String artist,Bitmap cover) {
        notiBuilder = new Notification.Builder(this,Constants.NOTIFICATION_CHANNEL_ID);
        Notification.MediaStyle style = new Notification.MediaStyle()
                .setMediaSession(token)
                .setShowActionsInCompactView(0);
        notiBuilder.setChannelId(Constants.NOTIFICATION_CHANNEL_ID)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setStyle(style)
                .setLargeIcon(cover)
                .setContentTitle(name)
                .setContentText(artist);
        notification = notiBuilder.build();
        notiMgr.notify(1,notification);
    }

    /**
     * MediaSession callback
     */
    private static class MediaSessionCallback extends MediaSession.Callback {

        @Override
        public void onPlay() {
            super.onPlay();

        }

        @Override
        public void onPause() {
            super.onPause();

        }
    }

    /**
     * Close WebSocket client
     * 关闭 WebSocket 客户端
     * @params null
     */
    private void closeWsClient() {
        try {
            if (wsClient != null) {
                wsClient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            wsClient = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeWsClient();
    }

}
