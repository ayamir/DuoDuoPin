package com.example.duoduopin.activity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.duoduopin.R;
import com.example.duoduopin.bean.MessageContentBean;
import com.example.duoduopin.tool.MyDBHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.activity.LoginActivity.idContent;
import static com.example.duoduopin.activity.LoginActivity.tokenContent;
import static com.example.duoduopin.tool.Constants.checkSysMsgUrl;

public class SysMsgCaseActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchMsg;
    private boolean isFromServer = true;

    private List<MessageContentBean> messageContent;
    private final ArrayList<HashMap<String, String>> sysMsgCasesFromServer = new ArrayList<>();
    private final ArrayList<HashMap<String, String>> sysMsgDetailedCasesFromServer = new ArrayList<>();

    private final ArrayList<HashMap<String, String>> sysMsgCasesFromDB = new ArrayList<>();
    private final ArrayList<HashMap<String, String>> sysMsgDetailedCasesFromDB = new ArrayList<>();

    private final MyDBHelper myDBHelper = new MyDBHelper(this, "DuoDuoPin.db", null, 1);
    private final Context mContext = this;

    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sysmsg_layout);

        listView = findViewById(R.id.sysMsgCase);
        swipeRefreshLayout = findViewById(R.id.sys_msg_swipe_refresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    int state = postCheckSysMessage();
                    if (state == 1) {
                        if (isFromServer) {
                            readFromServer();
                            showItems(sysMsgCasesFromServer, sysMsgDetailedCasesFromServer);
                        } else {
                            readFromDB();
                            showItems(sysMsgCasesFromDB, sysMsgDetailedCasesFromDB);
                        }
                        Toast.makeText(mContext, "加载系统消息成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, "加载系统消息失败！", Toast.LENGTH_SHORT).show();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            int state = postCheckSysMessage();
            if (state == 1) {
                showItems(sysMsgCasesFromServer, sysMsgDetailedCasesFromServer);
            } else {
                Toast.makeText(mContext, "遇到未知错误，请稍后再试！", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        switchMsg = findViewById(R.id.switch_msg);
        switchMsg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    isFromServer = false;
                    readFromDB();
                    showItems(sysMsgCasesFromDB, sysMsgDetailedCasesFromDB);
                } else {
                    isFromServer = true;
                    readFromServer();
                    showItems(sysMsgCasesFromServer, sysMsgDetailedCasesFromServer);
                }
            }
        });
    }

    private void showItems(ArrayList<HashMap<String, String>> cases, final ArrayList<HashMap<String, String>> dCases) {

        SimpleAdapter adapter = new SimpleAdapter(this, cases, R.layout.system_message_tip,
                new String[]{"title", "content", "time"},
                new int[]{R.id.sys_msg_title, R.id.sys_msg_content, R.id.sys_msg_time});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SQLiteDatabase db = myDBHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("messageId", dCases.get((int) id).get("messageId"));
                values.put("senderId", dCases.get((int) id).get("senderId"));
                values.put("billId", dCases.get((int) id).get("billId"));
                values.put("type", dCases.get((int) id).get("type"));
                values.put("time", dCases.get((int) id).get("time"));
                values.put("content", dCases.get((int) id).get("content"));
                db.insert("SysMsg", null, values);

                Intent toIntent = new Intent(view.getContext(), OneSysMsgCaseActivity.class);
                toIntent.putExtra("messageId", dCases.get((int) id).get("messageId"));
                toIntent.putExtra("senderId", dCases.get((int) id).get("senderId"));
                toIntent.putExtra("billId", dCases.get((int) id).get("billId"));
                toIntent.putExtra("messageType", dCases.get((int) id).get("type"));
                toIntent.putExtra("time", dCases.get((int) id).get("time"));
                toIntent.putExtra("content", dCases.get((int) id).get("content"));
                String isFromServerString;
                if (isFromServer) {
                    isFromServerString = "true";
                } else {
                    isFromServerString = "false";
                }
                toIntent.putExtra("isFromServer", isFromServerString);
                startActivity(toIntent);
            }
        });
    }

    private void readFromDB() {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        Cursor cursor = db.query("SysMsg", null, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> mapFromDB = new HashMap<>();
                mapFromDB.put("title", "系统消息");
                mapFromDB.put("content", cursor.getString(cursor.getColumnIndex("content")));
                mapFromDB.put("time", cursor.getString(cursor.getColumnIndex("time")));
                if (!sysMsgCasesFromDB.contains(mapFromDB)) {
                    sysMsgCasesFromDB.add(mapFromDB);
                }

                HashMap<String, String> dmapFromDB = new HashMap<>();
                dmapFromDB.put("messageId", cursor.getString(cursor.getColumnIndex("messageId")));
                dmapFromDB.put("senderId", cursor.getString(cursor.getColumnIndex("senderId")));
                dmapFromDB.put("billId", cursor.getString(cursor.getColumnIndex("billId")));
                dmapFromDB.put("type", cursor.getString(cursor.getColumnIndex("type")));
                dmapFromDB.put("time", cursor.getString(cursor.getColumnIndex("time")));
                dmapFromDB.put("content", cursor.getString(cursor.getColumnIndex("content")));
                if (!sysMsgDetailedCasesFromDB.contains(dmapFromDB)) {
                    sysMsgDetailedCasesFromDB.add(dmapFromDB);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void readFromServer() {
        for (MessageContentBean content : messageContent) {
            HashMap<String, String> map = new HashMap<>();
            map.put("title", "系统消息");
            map.put("content", content.getContent());
            map.put("time", content.getTime().replace('T', ' '));
            if (!sysMsgCasesFromServer.contains(map)) {
                sysMsgCasesFromServer.add(map);
            }

            HashMap<String, String> dmap = new HashMap<>();
            dmap.put("messageId", content.getMessageId());
            dmap.put("senderId", content.getSenderId());
            dmap.put("billId", content.getBillId());
            dmap.put("type", content.getType());
            dmap.put("time", content.getTime().replace('T', ' '));
            dmap.put("content", content.getContent());
            if (!sysMsgDetailedCasesFromServer.contains(dmap)) {
                sysMsgDetailedCasesFromServer.add(dmap);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int postCheckSysMessage() throws IOException, JSONException {
        final String TAG = "checkSysMessgae";

        int ret = 0;

        Request request = new Request.Builder()
                .url(checkSysMsgUrl)
                .header("token", idContent + "_" + tokenContent)
                .post(RequestBody.create(null, ""))
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        JSONObject jsonObject = new JSONObject(Objects.requireNonNull(response.body()).string());
        String codeString = jsonObject.getString("code");
        int code = Integer.parseInt(codeString);

        if (response.code() == 200) {
            if (code == 100) {
                messageContent = new Gson().fromJson(jsonObject.getString("content"), new TypeToken<List<MessageContentBean>>() {
                }.getType());
                if (messageContent != null) {
                    ret = 1;
                }
            } else {
                ret = 2;
            }
        } else {
            ret = -1;
        }
        return ret;
    }
}
