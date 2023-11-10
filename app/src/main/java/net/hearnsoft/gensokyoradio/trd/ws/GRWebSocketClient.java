package net.hearnsoft.gensokyoradio.trd.ws;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

import javax.net.ssl.SSLParameters;

public class GRWebSocketClient extends WebSocketClient {

    private static final String TAG = GRWebSocketClient.class.getSimpleName();

    public GRWebSocketClient(URI serverUri) {
        super(serverUri, new Draft_6455());
        Log.d(TAG, "Init WebSocket client.");
    }

    @Override
    protected void onSetSSLParameters(SSLParameters sslParameters) {
        //super.onSetSSLParameters(sslParameters);
    }

    @Override
    public void onOpen(ServerHandshake handShakeData) {
        Log.d(TAG, "Connection opened. get handshake: " + handShakeData.getHttpStatusMessage());
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "onMessage: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "onClose: code:" + code + ", reason:" + reason + ", isRemoteClose:" + remote);
    }

    @Override
    public void onError(Exception ex) {
        Log.e(TAG, "onError: " + ex.toString());
        ex.printStackTrace();
    }
}
