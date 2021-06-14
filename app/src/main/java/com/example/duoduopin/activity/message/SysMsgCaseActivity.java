package com.example.duoduopin.activity.message;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.duoduopin.R;
import com.example.duoduopin.pojo.SysMsgContent;
import com.example.duoduopin.tool.MyDBHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.activity.MainActivity.client;
import static com.example.duoduopin.activity.MainActivity.idContent;
import static com.example.duoduopin.activity.MainActivity.tokenContent;
import static com.example.duoduopin.handler.GeneralMsgHandler.ERROR;
import static com.example.duoduopin.handler.GeneralMsgHandler.SUCCESS;
import static com.example.duoduopin.tool.Constants.checkSysMsgUrl;

public class SysMsgCaseActivity extends AppCompatActivity {
    private final ArrayList<HashMap<String, String>> sysMsgCasesFromServer = new ArrayList<>();
    private final ArrayList<HashMap<String, String>> sysMsgDetailedCasesFromServer = new ArrayList<>();
    private final ArrayList<HashMap<String, String>> sysMsgCasesFromDB = new ArrayList<>();
    private final ArrayList<HashMap<String, String>> sysMsgDetailedCasesFromDB = new ArrayList<>();
    private final MyDBHelper myDBHelper = new MyDBHelper(this, "DuoDuoPin.db", null, 1);
    private final Context mContext = this;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchMsg;
    private boolean isFromServer = true;
    private ArrayList<SysMsgContent> sysMsgContentList;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sys_msg_layout);

        listView = findViewById(R.id.sysMsgCase);
        swipeRefreshLayout = findViewById(R.id.sys_msg_swipe_refresh);

        switchMsg = findViewById(R.id.switch_msg);
        switchMsg.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isChecked()) {
                isFromServer = false;
                checkRead();
            } else {
                isFromServer = true;
                checkUnRead();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            final String TAG = "pull-to-refresh";
            if (isFromServer) {
                Log.e(TAG, "onRefresh: isFromServer = " + isFromServer);
                checkUnRead();
            } else {
                Log.e(TAG, "onRefresh: isFromServer = " + isFromServer);
                checkRead();
            }
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void checkRead() {
        readFromDB(true, sysMsgCasesFromDB, sysMsgDetailedCasesFromDB);
        showItems(sysMsgCasesFromDB, sysMsgDetailedCasesFromDB);
    }

    private void showItems(ArrayList<HashMap<String, String>> cases, final ArrayList<HashMap<String, String>> dCases) {
        if (cases.isEmpty()) {
            Log.e("", "showItems: cases is empty");
        }

        if (dCases.isEmpty()) {
            Log.e("", "showItems: cases is empty");
        }

        SimpleAdapter adapter = new SimpleAdapter(this, cases, R.layout.tip_msg_sys,
                new String[]{"title", "content", "time"},
                new int[]{R.id.sys_msg_title, R.id.sys_msg_content, R.id.sys_msg_time});

        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            SQLiteDatabase db = myDBHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            String messageId = dCases.get((int) id).get("messageId");
            String senderId = dCases.get((int) id).get("senderId");
            String receiverId = dCases.get((int) id).get("receiverId");
            String billId = dCases.get((int) id).get("billId");
            String type = dCases.get((int) id).get("type");
            String time = dCases.get((int) id).get("time");
            String content = dCases.get((int) id).get("content");

            values.put("messageId", messageId);
            values.put("senderId", senderId);
            values.put("receiverId", receiverId);
            values.put("billId", billId);
            values.put("type", type);
            values.put("time", time);
            values.put("content", content);
            if (type != null) {
                if (type.equals("APPLY")) {
                    values.put("isRead", String.valueOf(false));
                } else {
                    values.put("isRead", String.valueOf(true));
                }
            }
            db.replace("SysMsg", null, values);

            String isRead = dCases.get((int) id).get("isRead");
            if (type != null) {
                Intent toIntent;
                if (type.equals("COMPL")) {
                    toIntent = new Intent(view.getContext(), OneSysMsgCaseActivity.class);
                    toIntent.putExtra("messageId", messageId);
                    toIntent.putExtra("senderId", senderId);
                    toIntent.putExtra("billId", billId);
                    toIntent.putExtra("messageType", type);
                    toIntent.putExtra("time", time);
                    toIntent.putExtra("content", content);
                } else {
                    toIntent = new Intent(view.getContext(), SysMsgCreditActivity.class);
                    toIntent.putExtra("groupId", billId);
                }
                toIntent.putExtra("isRead", isRead);
                startActivity(toIntent);
            }
        });
    }

    private void readFromDB(boolean isRead, ArrayList<HashMap<String, String>> cases, ArrayList<HashMap<String, String>> detailedCases) {
        cases.clear();
        detailedCases.clear();

        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        String[] args = new String[]{idContent, String.valueOf(isRead)};
        Cursor cursor = db.query("SysMsg", null, "receiverId=? and isRead=?", args, null, null, "time desc", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> mapFromDB = new HashMap<>();
                mapFromDB.put("title", "系统消息");
                mapFromDB.put("content", cursor.getString(cursor.getColumnIndex("content")));
                mapFromDB.put("time", cursor.getString(cursor.getColumnIndex("time")));
                cases.add(mapFromDB);

                HashMap<String, String> dmapFromDB = new HashMap<>();
                dmapFromDB.put("messageId", cursor.getString(cursor.getColumnIndex("messageId")));
                dmapFromDB.put("senderId", cursor.getString(cursor.getColumnIndex("senderId")));
                dmapFromDB.put("receiverId", cursor.getString(cursor.getColumnIndex("receiverId")));
                dmapFromDB.put("billId", cursor.getString(cursor.getColumnIndex("billId")));
                dmapFromDB.put("type", cursor.getString(cursor.getColumnIndex("type")));
                dmapFromDB.put("time", cursor.getString(cursor.getColumnIndex("time")));
                dmapFromDB.put("content", cursor.getString(cursor.getColumnIndex("content")));
                dmapFromDB.put("isRead", cursor.getString(cursor.getColumnIndex("isRead")));
                detailedCases.add(dmapFromDB);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkUnRead() {
        sysMsgDetailedCasesFromServer.clear();
        sysMsgCasesFromServer.clear();

        if (sysMsgContentList != null) {
            sysMsgContentList.clear();
        }

        readFromDB(false, sysMsgCasesFromServer, sysMsgDetailedCasesFromServer);

        @SuppressLint("HandlerLeak") final Handler checkSysMsgHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == SUCCESS) {
                    Toast.makeText(mContext, "从云端加载消息成功！", Toast.LENGTH_SHORT).show();
                    for (SysMsgContent content : sysMsgContentList) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("title", "系统消息");
                        String contentString = content.getContent();
                        map.put("content", contentString);
                        map.put("time", content.getTime());
                        sysMsgCasesFromServer.add(map);

                        HashMap<String, String> dmap = new HashMap<>();
                        dmap.put("messageId", content.getMessageId());
                        dmap.put("senderId", content.getSenderId());
                        dmap.put("receiverId", content.getReceiverId());
                        dmap.put("billId", content.getBillId());
                        dmap.put("type", content.getType());
                        dmap.put("time", content.getTime());
                        dmap.put("content", contentString);
                        dmap.put("isRead", String.valueOf(content.isRead()));
                        sysMsgDetailedCasesFromServer.add(dmap);
                    }
                    showItems(sysMsgCasesFromServer, sysMsgDetailedCasesFromServer);
                } else {
                    Toast.makeText(mContext, "遇到未知错误，请稍后再试！", Toast.LENGTH_SHORT).show();
                }
            }
        };

        new Thread(() -> {
            Message message = new Message();
            try {
                message.what = postCheckSysMessage();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            checkSysMsgHandler.sendMessage(message);
        }).start();
    }

    private void insertToDB(SysMsgContent content) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("messageId", content.getMessageId());
        values.put("senderId", content.getSenderId());
        values.put("receiverId", content.getReceiverId());
        values.put("billId", content.getBillId());
        values.put("type", content.getType());
        values.put("time", content.getTime());
        values.put("content", content.getContent());
        values.put("isRead", String.valueOf(content.isRead()));
        db.insert("SysMsg", null, values);
        Log.e("", "insertToDB: insert success, content = " + content.toString());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private int postCheckSysMessage() throws IOException, JSONException {
        final String TAG = "checkSysMessage";
        int ret = 0;

        Request request = new Request.Builder()
                .url(checkSysMsgUrl)
                .header("token", idContent + "_" + tokenContent)
                .post(RequestBody.create(null, ""))
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        String responseString = Objects.requireNonNull(response.body()).string();
        Log.e(TAG, "postCheckSysMessage: responseString = " + responseString);
        JSONObject jsonObject = new JSONObject(responseString);
        String codeString = jsonObject.getString("code");
        int code = Integer.parseInt(codeString);

        if (response.code() == 200) {
            if (code == 100) {
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                sysMsgContentList = gson.fromJson(jsonObject.getString("content"), new TypeToken<List<SysMsgContent>>() {
                }.getType());
                if (sysMsgContentList != null) {
                    ret = SUCCESS;
                    for (SysMsgContent content : sysMsgContentList) {
                        long oldTime = Long.parseLong(content.getTime());
                        String newTime = Instant.ofEpochMilli(oldTime).atZone(ZoneOffset.ofHours(8)).toLocalDateTime().toString().replace('T', ' ');
                        content.setTime(newTime);
                        content.setRead(false);
                        Log.e(TAG, "postCheckSysMessage: content is " + content.toString());
                        insertToDB(content);
                    }
                }
            }
        } else {
            ret = ERROR;
        }
        return ret;
    }
}
