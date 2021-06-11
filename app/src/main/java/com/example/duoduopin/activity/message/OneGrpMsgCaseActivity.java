package com.example.duoduopin.activity.message;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
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
import com.example.duoduopin.adapter.GrpMsgAdapter;
import com.example.duoduopin.bean.BriefMemberInfo;
import com.example.duoduopin.bean.GrpMsgContent;
import com.example.duoduopin.bean.GrpMsgDisplay;
import com.example.duoduopin.tool.MyDBHelper;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import okhttp3.WebSocket;

import static com.example.duoduopin.activity.MainActivity.idContent;
import static com.example.duoduopin.activity.MainActivity.nicknameContent;
import static com.example.duoduopin.fragment.main.MessageFragment.recGrpMsgService;
import static com.example.duoduopin.tool.Constants.group_new_msg_signal;

public class OneGrpMsgCaseActivity extends AppCompatActivity {
    private String grpId;
    private String grpTitle;
    private ArrayList<BriefMemberInfo> memberInfoList;

    private EditText msgInput;
    private LinearLayoutManager grpMsgLayoutManager;
    private SwipeRefreshLayout grpMsgSwipeRefresh;

    private final ArrayList<GrpMsgDisplay> grpMsgDisplayList = new ArrayList<>();
    private final GrpMsgAdapter grpMsgAdapter = new GrpMsgAdapter(grpMsgDisplayList);

    private final MyDBHelper myDBHelper = new MyDBHelper(this, "DuoDuoPin.db", null, 1);

    private WebSocket grpMsgWebSocket;

    private class GrpMsgReceiverDisplay extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            GrpMsgContent newMsg = (GrpMsgContent) intent.getSerializableExtra("newMsg");

            // Just handle UI
            displayNewMsg(newMsg, newMsg.getUserId().equals(idContent));
            grpMsgLayoutManager.scrollToPosition(grpMsgAdapter.getItemCount() - 1);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_grpmsg_case);

        getInfoFromIntent();
        bindItemsAndOps();

        loadHistoryMsgs();

        doRegisterReceiver(this);
    }

    private void doRegisterReceiver(Context context) {
        GrpMsgReceiverDisplay grpMsgReceiverDisplay = new GrpMsgReceiverDisplay();
        IntentFilter intentFilter = new IntentFilter(group_new_msg_signal);
        context.registerReceiver(grpMsgReceiverDisplay, intentFilter);
    }

    private void getInfoFromIntent() {
        Intent fromIntent = getIntent();
        if (fromIntent != null) {
            grpId = fromIntent.getStringExtra("grpId");
            grpTitle = fromIntent.getStringExtra("grpTitle");
        }
    }

    private void getMemberInfoList() {
        // TODO: get member info list from server
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void bindItemsAndOps() {
        ImageView ivGroupDetails = findViewById(R.id.iv_group_details);
        ivGroupDetails.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), GrpDetailsActivity.class);
            intent.putExtra("orderId", grpId);
            intent.putExtra("groupTitle", grpTitle);
            intent.putExtra("memberInfoList", memberInfoList);
            v.getContext().startActivity(intent);
        });

        if (grpId != null && recGrpMsgService != null) {
            grpMsgWebSocket = recGrpMsgService.getWebSocketMap().get(grpId);
        }

        TextView grpTitleView = findViewById(R.id.one_grp_title);
        if (grpTitle != null) {
            grpTitleView.setText(grpTitle);
        }

        ImageView backButton = findViewById(R.id.sysmsg_back_btn);
        backButton.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });

        grpMsgLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView grpMsgRecyclerview = findViewById(R.id.grp_msg_recyclerview);
        grpMsgRecyclerview.setLayoutManager(grpMsgLayoutManager);
        grpMsgRecyclerview.setAdapter(grpMsgAdapter);

        grpMsgSwipeRefresh = findViewById(R.id.grp_msg_swipe_refresh);
        grpMsgSwipeRefresh.setOnRefreshListener(() -> {
            loadHistoryMsgs();
            grpMsgSwipeRefresh.setRefreshing(false);
        });

        msgInput = findViewById(R.id.msg_input);
        Button msgSendButton = findViewById(R.id.msg_send_button);
        msgSendButton.setOnClickListener(v -> {
            String msgInputString = msgInput.getText().toString();
            if (!msgInputString.isEmpty()) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                String nowTime = dtf.format(now);
                String nowTimeToServer = nowTime.replace(' ', 'T');
                final GrpMsgContent msgContent = new GrpMsgContent(idContent, grpId, grpTitle, nicknameContent, "CHAT", nowTimeToServer, msgInputString);
                if (grpMsgWebSocket != null) {
                    grpMsgWebSocket.send(new Gson().toJson(msgContent));
                }
                msgInput.setText("");
            }
        });
    }

    private void loadHistoryMsgs() {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        String[] args = new String[]{grpId};
        @SuppressLint("Recycle") Cursor cursor = db.query("GrpMsg", new String[]{"userId", "nickname", "content", "time"}, "groupId = ?", args, null, null, "time ASC", null);
        if (cursor.moveToFirst()) {
            do {
                // query from DB
                String userId = cursor.getString(cursor.getColumnIndex("userId"));
                String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                boolean isMine = false;
                if (userId.equals(idContent)) {
                    isMine = true;
                }
                // handle UI
                GrpMsgDisplay grpMsgDisplay = new GrpMsgDisplay(content, R.drawable.testperson, nickname, time, isMine);
                if (!grpMsgDisplayList.contains(grpMsgDisplay)) {
                    grpMsgAdapter.addHistory(grpMsgDisplay);
                    grpMsgLayoutManager.scrollToPosition(grpMsgAdapter.getItemCount() - 1);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void displayNewMsg(GrpMsgContent msgContent, boolean isMine) {
        String nickname = msgContent.getNickname();
        String content = msgContent.getContent();
        String time = msgContent.getTime().replace('T', ' ');
        GrpMsgDisplay msgDisplay = new GrpMsgDisplay(content, R.drawable.testperson, nickname, time, isMine);
        if (!grpMsgDisplayList.contains(msgDisplay)) {
            grpMsgAdapter.add(msgDisplay);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_OK);
            finish();
        }

        return super.onKeyDown(keyCode, event);
    }
}
