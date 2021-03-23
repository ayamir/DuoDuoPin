package com.example.duoduopin.tool;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class JWebSocketClient extends WebSocketClient {
    private final String TAG = "JWebSocketClient";
    public JWebSocketClient(URI serverUri) {
        super(serverUri, new Draft_6455());
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        send("Hello, this is client");
        Log.d(TAG, "onOpen: new connection opened");
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "onMessage: received message = " + message);

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "close with exit code = " + code + " additional info = " + reason);
    }

    @Override
    public void onError(Exception ex) {
        Log.e(TAG, "onError: a error occurred = " + ex);
    }
}
