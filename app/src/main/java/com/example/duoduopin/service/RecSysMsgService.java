package com.example.duoduopin.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.duoduopin.bean.SysMsgContent;
import com.example.duoduopin.tool.MyDBHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneOffset;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import static com.example.duoduopin.activity.MainActivity.idContent;
import static com.example.duoduopin.activity.MainActivity.tokenContent;
import static com.example.duoduopin.service.RecGrpMsgService.socketClient;
import static com.example.duoduopin.tool.Constants.getSysMsgUrl;

public class RecSysMsgService extends Service {
    private final MyDBHelper myDBHelper = new MyDBHelper(this, "DuoDuoPin.db", null, 1);

    public class RecSysMsgBinder extends Binder {
        public RecSysMsgService getService() {
            return RecSysMsgService.this;
        }
    }

    private final RecSysMsgBinder sysMsgBinder = new RecSysMsgBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sysMsgBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Request request = new Request.Builder()
                .url(getSysMsgUrl(idContent))
                .header("token", idContent + "_" + tokenContent)
                .build();
        final String TAG = "SysMsg WebSocket";
        socketClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull final WebSocket webSocket, @NotNull Response response) {
                super.onOpen(webSocket, response);
                Log.e(TAG, "onOpen: SysMsg WebSocket opened!");
            }

            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosed(webSocket, code, reason);
                Log.e(TAG, "onOpen: SysMsg WebSocket closed, because of " + code + "/" + reason);
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosing(webSocket, code, reason);
                Log.e(TAG, "onOpen: SysMsg WebSocket is closing, because of " + code + "/" + reason);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @org.jetbrains.annotations.Nullable Response response) {
                super.onFailure(webSocket, t, response);
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                super.onMessage(webSocket, text);
                Log.e(TAG, "onMessage: new message is " + text);
                insertToDB(text);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void insertToDB(String text) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        SysMsgContent content = gson.fromJson(text, SysMsgContent.class);
        long oldTime = Long.parseLong(content.getTime());
        String newTime = Instant.ofEpochMilli(oldTime).atZone(ZoneOffset.ofHours(8)).toLocalDateTime().toString().replace('T', ' ');
        content.setTime(newTime);
        content.setRead(false);
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("messageId", content.getMessageId());
        values.put("senderId", content.getSenderId());
        values.put("receiverId", content.getReceiverId());
        values.put("billId", content.getBillId());
        values.put("type", content.getType());
        values.put("time", content.getTime());
        values.put("content", content.getContent());
        values.put("isRead", content.isRead());
        db.insert("SysMsg", null, values);
        Log.e("", "insertToDB: insert success, content = " + content.toString());
    }
}
