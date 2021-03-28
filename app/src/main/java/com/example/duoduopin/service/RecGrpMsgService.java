package com.example.duoduopin.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.duoduopin.bean.BriefGrpMsg;
import com.example.duoduopin.bean.GrpMsgContent;
import com.example.duoduopin.bean.OrderContent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static com.example.duoduopin.activity.LoginActivity.idContent;
import static com.example.duoduopin.activity.LoginActivity.tokenContent;
import static com.example.duoduopin.activity.MainActivity.client;
import static com.example.duoduopin.tool.Constants.getChatUrl;
import static com.example.duoduopin.tool.Constants.getQueryUrlByUserId;

public class RecGrpMsgService extends Service {
    private ArrayList<OrderContent> orderContent;
    private final ArrayList<String> grpIdList = new ArrayList<>();

    private final OkHttpClient socketClient = new OkHttpClient().newBuilder()
            .readTimeout(0, TimeUnit.SECONDS)
            .writeTimeout(0, TimeUnit.SECONDS)
            .connectTimeout(0, TimeUnit.SECONDS)
            .pingInterval(10, TimeUnit.SECONDS)
            .build();

    private final RecGrpMsgBinder mBinder = new RecGrpMsgBinder();

    public class RecGrpMsgBinder extends Binder {
        public RecGrpMsgService getService() {
            return RecGrpMsgService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate() {
        super.onCreate();
        loadGrpTips();
    }

    public ArrayList<String> getGrpIdList() {
        return grpIdList;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void loadGrpTips() {
        final String TAG = "loadGrpTips";
        try {
            int state = postQueryOrdersByUserId();
            if (state == 1) {
                for (OrderContent content : orderContent) {
                    String grpId = content.getBillId();
                    if (!grpIdList.contains(grpId)) {
                        grpIdList.add(grpId);
                    }
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        for (final String grpId : grpIdList) {
            Request request = new Request.Builder()
                    .url(getChatUrl(grpId))
                    .header("token", idContent + "_" + tokenContent)
                    .build();
            socketClient.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                    super.onClosed(webSocket, code, reason);
                    Log.e(TAG, grpId + "'s websocket is closed due to " + code + "/" + reason);
                }

                @Override
                public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                    super.onClosing(webSocket, code, reason);
                    Log.e(TAG, grpId + "'s websocket is closing due to " + code + "/" + reason);
                }

                @Override
                public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @org.jetbrains.annotations.Nullable Response response) {
                    super.onFailure(webSocket, t, response);
                    Log.e(TAG, "onFailure: failure!");
                }

                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                    Log.d(TAG, "onMessage: new message is " + text);
                    GrpMsgContent newMsg = new Gson().fromJson(text, GrpMsgContent.class);
                    long oldTime = Long.parseLong(newMsg.getTime());
                    newMsg.setTime(Instant.ofEpochMilli(oldTime).atZone(ZoneOffset.ofHours(8)).toLocalDateTime().toString().replace('T', ' '));

                    // Convert to brief message to display on message fragment
                    BriefGrpMsg briefGrpMsg = convertToBrief(newMsg);

                    // Broadcast brief message to display on UI thread
                    Intent intent = new Intent();
                    intent.putExtra("briefGrpMsg", briefGrpMsg);
                    intent.putExtra("grpId", newMsg.getBillId());
                    intent.setAction("com.example.duoduopin.grpmsg.brief");
                    sendBroadcast(intent);
                }

                @Override
                public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                    super.onMessage(webSocket, bytes);
                }

                @Override
                public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                    super.onOpen(webSocket, response);
                    Log.e(TAG, "websocket for " + grpId + " opened!");
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int postQueryOrdersByUserId() throws IOException, JSONException {
        int ret = 0;

        Request request = new Request.Builder()
                .url(getQueryUrlByUserId(idContent))
                .header("token", idContent + "_" + tokenContent)
                .post(RequestBody.create(null, ""))
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            JSONObject responseJSON = new JSONObject(Objects.requireNonNull(response.body()).string());
            orderContent = new Gson().fromJson(responseJSON.getString("content"), new TypeToken<List<OrderContent>>() {
            }.getType());
            if (orderContent != null) {
                ret = 1;
            }
        } else {
            ret = -1;
        }

        return ret;
    }

    private BriefGrpMsg convertToBrief(GrpMsgContent newMsg) {
        String grpTitle = newMsg.getBillTitle();
        String msgOwnerNickname = newMsg.getNickname();
        String content = newMsg.getContent();
        String time = newMsg.getTime();

        return new BriefGrpMsg(grpTitle, msgOwnerNickname, content, time);
    }
}