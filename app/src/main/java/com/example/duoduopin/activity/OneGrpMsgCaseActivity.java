package com.example.duoduopin.activity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.duoduopin.R;
import com.example.duoduopin.bean.GrpMsgContent;
import com.example.duoduopin.bean.GrpMsgDisplay;
import com.example.duoduopin.tool.MyDBHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static com.example.duoduopin.activity.LoginActivity.idContent;
import static com.example.duoduopin.activity.LoginActivity.nicknameContent;
import static com.example.duoduopin.activity.LoginActivity.tokenContent;
import static com.example.duoduopin.tool.Constants.getChatUrl;

public class OneGrpMsgCaseActivity extends AppCompatActivity {
    private String grpId;
    private String grpTitle;
    private WebSocket mWebSocket;

    private MyDBHelper myDBHelper = new MyDBHelper(this, "DuoDuoPin.db", null, 1);

    private ArrayList<GrpMsgDisplay> grpMsgDisplayList = new ArrayList<>();

    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .pingInterval(10, TimeUnit.SECONDS)
            .build();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        final String TAG = "onCreate";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_grpmsg_case);

        getGrpMsg();

        TextView grpTitleView = findViewById(R.id.one_grp_title);

        ImageView backButton = findViewById(R.id.sysmsg_back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RecyclerView grpMsgs = findViewById(R.id.grp_msg_recyclerview);

        final SwipeRefreshLayout grpMsgSwipeRefresh = findViewById(R.id.grp_msg_swipe_refresh);
        grpMsgSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryFromDB();
                grpMsgSwipeRefresh.setRefreshing(false);
            }
        });

        Intent fromIntent = getIntent();
        if (fromIntent != null) {
            grpId = fromIntent.getStringExtra("grpId");
            grpTitle = fromIntent.getStringExtra("grpTitle");
            grpTitleView.setText(grpTitle);
        }

        final EditText msgInput = findViewById(R.id.msg_input);
        Button msgSendButton = findViewById(R.id.msg_send_button);
        msgSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msgInputString = msgInput.getText().toString();
                if (!msgInputString.isEmpty()) {
                    boolean isSended = mWebSocket.send(msgInputString);
                    if (isSended) {
                        GrpMsgContent content = new GrpMsgContent(idContent, grpId, grpTitle, nicknameContent, "CHAT", Calendar.getInstance().getTime().toString(), msgInputString);
                        insertToDB(content);
                    }
                }
            }
        });
    }

    private void queryFromDB() {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        String[] args = {grpId};
        @SuppressLint("Recycle") Cursor cursor = db.query("GrpMsg", new String[]{"nickname", "content", "time"}, "groupId=?", args, null, null, "time", "6");
        if (cursor.moveToFirst()) {
            do {
                String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                GrpMsgDisplay grpMsgDisplay = new GrpMsgDisplay(content, R.drawable.testperson, nickname, time);
                grpMsgDisplayList.add(grpMsgDisplay);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void insertToDB(GrpMsgContent content) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("groupId", content.getBillId());
        values.put("groupTitle", content.getBillTitle());
        values.put("userId", content.getUserId());
        values.put("ownerId", idContent);
        values.put("content", content.getContent());
        values.put("time", content.getTime());
        db.insert("GrpMsg", null, values);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getGrpMsg() {
        final String TAG = "socket request";
        Request socketRequest = new Request.Builder()
                .url(getChatUrl(grpId))
                .header("token", idContent + "_" + tokenContent)
                .get()
                .build();
        mWebSocket = client.newWebSocket(socketRequest, new WebSocketListener() {
            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosed(webSocket, code, reason);
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @org.jetbrains.annotations.Nullable Response response) {
                super.onFailure(webSocket, t, response);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                super.onMessage(webSocket, text);
                List<GrpMsgContent> grpMsgContentList = new Gson().fromJson(text, new TypeToken<List<GrpMsgContent>>() {
                }.getType());
                for (GrpMsgContent grpMsgContent : grpMsgContentList) {
                    insertToDB(grpMsgContent);
                }
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                super.onMessage(webSocket, bytes);
            }

            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                super.onOpen(webSocket, response);
            }
        });
    }
}
