package com.example.duoduopin.service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.example.duoduopin.tool.JWebSocketClient;

import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;

import java.net.URI;

public class JWebSocketClientService extends JWebSocketClient {
    public JWebSocketClient client;
    private JWebSocketClientBinder mBinder = new JWebSocketClientBinder();

    public JWebSocketClientService(URI serverUri) {
        super(serverUri);
    }

    public class JWebSocketClientBinder extends Binder {
        public JWebSocketClientService getService() {
            return JWebSocketClientService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
