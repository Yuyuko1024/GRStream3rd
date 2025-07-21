package net.hearnsoft.gr3rd.compose.socket

import net.hearnsoft.gr3rd.compose.utils.Logger
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import javax.net.ssl.SSLParameters

class GRWebSocketClient(
    serverUri: URI,
    private val onMessage: (String) -> Unit = {},
    private val onClose: (Int, String, Boolean) -> Unit = { _, _, _ -> },
    private val onOpen: (ServerHandshake) -> Unit = {},
    private val onError: (Exception) -> Unit = {}
) : WebSocketClient(serverUri, Draft_6455()) {
    override fun onSetSSLParameters(sslParameters: SSLParameters?) {

    }

    override fun onOpen(handshake: ServerHandshake) {
        Logger.info(this, "WebSocket connection opened: $handshake")
        onOpen.invoke(handshake)
    }

    override fun onMessage(message: String) {
        Logger.info(this, "Received message: $message")
        onMessage.invoke(message)
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        Logger.info(this, "WebSocket connection closed: code=$code, reason=$reason, remote=$remote")
        onClose.invoke(code, reason, remote)
    }

    override fun onError(ex: Exception) {
        Logger.err(this, "WebSocket error occurred", ex)
        onError.invoke(ex)
    }

}