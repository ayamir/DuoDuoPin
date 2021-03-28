package com.example.duoduopin.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.duoduopin.R;
import com.example.duoduopin.bean.GrpMsgContent;
import com.example.duoduopin.bean.GrpMsgDisplay;
import com.example.duoduopin.tool.GrpMsgAdapter;
import com.example.duoduopin.tool.MyDBHelper;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import static com.example.duoduopin.activity.LoginActivity.idContent;
import static com.example.duoduopin.activity.LoginActivity.nicknameContent;
import static com.example.duoduopin.activity.LoginActivity.tokenContent;
import static com.example.duoduopin.tool.Constants.getChatUrl;

public class OneGrpMsgCaseActivity extends AppCompatActivity {
    private String grpId;
    private String grpTitle;

    private EditText msgInput;
    private Button msgSendButton;
    private TextView grpTitleView;
    private RecyclerView grpMsgRecyclerview;
    private SwipeRefreshLayout grpMsgSwipeRefresh;

    private final ArrayList<GrpMsgDisplay> grpMsgDisplayList = new ArrayList<>();
    private final GrpMsgAdapter grpMsgAdapter = new GrpMsgAdapter(grpMsgDisplayList);

    private final MyDBHelper myDBHelper = new MyDBHelper(this, "DuoDuoPin.db", null, 1);

    private final OkHttpClient websocketClient = new OkHttpClient().newBuilder()
            .readTimeout(0, TimeUnit.SECONDS)
            .writeTimeout(0, TimeUnit.SECONDS)
            .connectTimeout(0, TimeUnit.SECONDS)
            .pingInterval(10, TimeUnit.SECONDS)
            .build();

    private Request socketRequest;
    private WebSocket grpMsgWebSocket;

    private class GrpMsgReceiverDisplay extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            GrpMsgContent newMsg = (GrpMsgContent) intent.getSerializableExtra("msgToDisplay");
            displayMsg(newMsg, newMsg.getUserId().equals(idContent));
            insertToDB(newMsg);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        final String TAG = "onCreate";
        super.onCreate(savedInstanceState);

        getInfoFromIntent();
        bindItemsAndOps();

        queryFromDB();

        GrpMsgReceiverDisplay grpMsgReceiverDisplay = new GrpMsgReceiverDisplay();
        IntentFilter intentFilter = new IntentFilter("com.example.duoduopin.grpmsg.display");
        this.registerReceiver(grpMsgReceiverDisplay, intentFilter);
    }

    private void getInfoFromIntent() {
        Intent fromIntent = getIntent();
        if (fromIntent != null) {
            grpId = fromIntent.getStringExtra("grpId");
            grpTitle = fromIntent.getStringExtra("grpTitle");
        }
    }

    private void bindItemsAndOps() {
        setContentView(R.layout.activity_one_grpmsg_case);

        if (grpId != null) {
            socketRequest = new Request.Builder()
                    .url(getChatUrl(grpId))
                    .header("token", idContent + "_" + tokenContent)
                    .build();
        }

        grpTitleView = findViewById(R.id.one_grp_title);
        if (grpTitle != null) {
            grpTitleView.setText(grpTitle);
        }

        ImageView backButton = findViewById(R.id.sysmsg_back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        grpMsgRecyclerview = findViewById(R.id.grp_msg_recyclerview);
        grpMsgRecyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        grpMsgRecyclerview.setAdapter(grpMsgAdapter);

        grpMsgSwipeRefresh = findViewById(R.id.grp_msg_swipe_refresh);
        grpMsgSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryFromDB();
                grpMsgSwipeRefresh.setRefreshing(false);
            }
        });

        msgInput = findViewById(R.id.msg_input);
        msgSendButton = findViewById(R.id.msg_send_button);
        msgSendButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String msgInputString = msgInput.getText().toString();
                if (!msgInputString.isEmpty()) {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    String nowTime = dtf.format(now);
                    String nowTimeToServer = nowTime.replace(' ', 'T');
                    final GrpMsgContent msgContent = new GrpMsgContent(idContent, grpId, grpTitle, nicknameContent, "CHAT", nowTimeToServer, msgInputString);
                    grpMsgWebSocket.send(new Gson().toJson(msgContent));
                    msgInput.setText("");
                }
            }
        });

        grpMsgWebSocket = websocketClient.newWebSocket(socketRequest, new WebSocketListener() {
            final String TAG = "grpMsgWebSocket";

            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                Log.e(TAG, "onOpen: grpMsgWebSocket opened!");
            }

            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosed(webSocket, code, reason);
                Log.e(TAG, "onClosed: grpMsgWebSocket closed due to" + code + "/" + reason);
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosing(webSocket, code, reason);
                Log.e(TAG, "onClosing: grpMsgWebSocket is closing due to" + code + "/" + reason);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @org.jetbrains.annotations.Nullable Response response) {
                super.onFailure(webSocket, t, response);
                Log.e(TAG, "onFailure: Failure!" + t.getClass());
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                Log.e(TAG, "onMessage: " + text);
                GrpMsgContent newMsg = new Gson().fromJson(text, GrpMsgContent.class);
                long oldTime = Long.parseLong(newMsg.getTime());
                newMsg.setTime(Instant.ofEpochMilli(oldTime).atZone(ZoneOffset.ofHours(8)).toLocalDateTime().toString().replace('T', ' '));
                Log.e(TAG, "onMessage: " + newMsg.toString());
                Intent intent = new Intent();
                intent.putExtra("msgToDisplay", newMsg);
                intent.setAction("com.example.duoduopin.grpmsg.display");
                sendBroadcast(intent);
            }
        });
    }

    private void queryFromDB() {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        String[] args = new String[]{grpId};
        @SuppressLint("Recycle") Cursor cursor = db.query("GrpMsg", new String[]{"userId", "nickname", "content", "time"}, "groupId = ?", args, null, null, "time ASC", null);
        if (cursor.moveToFirst()) {
            do {
                String userId = cursor.getString(cursor.getColumnIndex("userId"));
                String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                boolean isMine = false;
                if (userId.equals(idContent)) {
                    isMine = true;
                }
                GrpMsgDisplay grpMsgDisplay = new GrpMsgDisplay(content, R.drawable.testperson, nickname, time, isMine);
                if (!grpMsgDisplayList.contains(grpMsgDisplay)) {
                    grpMsgAdapter.addHistory(grpMsgDisplay);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void insertToDB(GrpMsgContent msgContent) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("groupId", msgContent.getBillId());
        values.put("groupTitle", msgContent.getBillTitle());
        values.put("userId", msgContent.getUserId());
        values.put("ownerId", idContent);
        values.put("nickname", msgContent.getNickname());
        values.put("content", msgContent.getContent());
        values.put("time", msgContent.getTime().replace('T', ' '));
        db.insertWithOnConflict("GrpMsg", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private void displayMsg(GrpMsgContent msgContent, boolean isMine) {
        String nickname = msgContent.getNickname();
        String content = msgContent.getContent();
        String time = msgContent.getTime().replace('T', ' ');
        GrpMsgDisplay msgDisplay = new GrpMsgDisplay(content, R.drawable.testperson, nickname, time, isMine);
        if (!grpMsgDisplayList.contains(msgDisplay)) {
            grpMsgAdapter.add(msgDisplay);
        }
    }
}
