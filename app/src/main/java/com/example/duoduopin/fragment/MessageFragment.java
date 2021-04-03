package com.example.duoduopin.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.duoduopin.R;
import com.example.duoduopin.activity.OneGrpMsgCaseActivity;
import com.example.duoduopin.activity.SysMsgCaseActivity;
import com.example.duoduopin.bean.BriefGrpMsg;
import com.example.duoduopin.bean.GrpMsgContent;
import com.example.duoduopin.service.RecMsgService;
import com.example.duoduopin.tool.MyDBHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.example.duoduopin.activity.MainActivity.client;
import static com.example.duoduopin.activity.MainActivity.idContent;
import static com.example.duoduopin.activity.MainActivity.tokenContent;
import static com.example.duoduopin.handler.GeneralMsgHandler.ERROR;
import static com.example.duoduopin.handler.GeneralMsgHandler.SUCCESS;
import static com.example.duoduopin.tool.Constants.getOfflineMessageUrl;
import static com.example.duoduopin.tool.Constants.getQueryChatMessageUrl;
import static com.example.duoduopin.tool.Constants.group_id_loaded_signal;
import static com.example.duoduopin.tool.Constants.group_new_msg_signal;
import static com.example.duoduopin.tool.Constants.group_quit_signal;

public class MessageFragment extends Fragment {
    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<String> grpIdList;
    private final HashMap<String, ArrayList<GrpMsgContent>> allGrpsMsgListMap = new HashMap<>();
    private final HashMap<String, BriefGrpMsg> briefGrpMsgMap = new HashMap<>();

    private MyDBHelper myDBHelper;
    public static RecMsgService recMsgService;

    private GrpMsgReceiverBrief grpMsgReceiverBrief;
    private GrpQuitReceiver grpQuitReceiver;
    private GrpIdLoadedReceiver grpIdLoadedReceiver;

    @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == SUCCESS) {
                for (Map.Entry<String, ArrayList<GrpMsgContent>> entry : allGrpsMsgListMap.entrySet()) {
                    ArrayList<GrpMsgContent> msgList = entry.getValue();
                    for (GrpMsgContent msgContent : msgList) {
                        Log.d("onServiceConnected", "insert for grpId = " + entry.getKey());
                        insertToDB(msgContent);
                    }
                }

                // Query From DB and Display Latest Message
                for (String grpId : grpIdList) {
                    Log.d("onServiceConnected", "query for grpId = " + grpId);
                    queryFromDB(grpId);
                }
                showItems();
            }
        }
    };

    private final ServiceConnection connection = new ServiceConnection() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            // Bind service
            RecMsgService.RecGrpMsgBinder binder = (RecMsgService.RecGrpMsgBinder) iBinder;
            recMsgService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("MessageFragment", "onServiceDisconnected");
        }
    };

    private class GrpMsgReceiverBrief extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            GrpMsgContent newMsg = (GrpMsgContent) intent.getSerializableExtra("newMsg");

            // Insert to DB
            insertToDB(newMsg);
            queryFromDB(newMsg.getBillId());
            if (context == getActivity()) {
                showItems();
            }
        }
    }

    private class GrpQuitReceiver extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onReceive(Context context, Intent intent) {
            String quitGrpId = intent.getStringExtra("quitGrpId");

            Objects.requireNonNull(recMsgService.getWebSocketMap().get(quitGrpId)).close(1000, "You have quited " + quitGrpId + " group!");
            recMsgService.getWebSocketMap().remove(quitGrpId);
            briefGrpMsgMap.remove(quitGrpId);
            if (context == getActivity())
                showItems();
            cleanDB(quitGrpId);
        }
    }

    private class GrpIdLoadedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int signal = intent.getIntExtra("grpIdList", ERROR);
            if (signal == SUCCESS) {
                final String TAG = "grpIdList";
                grpIdList = recMsgService.getGrpIdList();

                if (grpIdList != null) {
                    for (String id : grpIdList) {
                        Log.e(TAG, "id = " + id);
                    }
                    // Get full message records from server
                    new Thread(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void run() {
                            Message message = new Message();
                            message.what = checkOfflineMsg(false);
                            handler.sendMessage(message);
                        }
                    }).start();
                    Log.e("MessageFragment", "onServiceConnected");
                } else {
                    Log.e(TAG, "grpIdList == null");
                }
            }
        }
    }

    private void doRegisterReceiver() {
        grpMsgReceiverBrief = new GrpMsgReceiverBrief();
        IntentFilter msgFilter = new IntentFilter(group_new_msg_signal);
        getActivity().registerReceiver(grpMsgReceiverBrief, msgFilter);

        grpQuitReceiver = new GrpQuitReceiver();
        IntentFilter quitFilter = new IntentFilter(group_quit_signal);
        getActivity().registerReceiver(grpQuitReceiver, quitFilter);

        grpIdLoadedReceiver = new GrpIdLoadedReceiver();
        IntentFilter loadFilter = new IntentFilter(group_id_loaded_signal);
        getActivity().registerReceiver(grpIdLoadedReceiver, loadFilter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_message, null);
        return view;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (this.getView() != null) {
            this.getView().setVisibility(menuVisible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        myDBHelper = new MyDBHelper(getActivity(), "DuoDuoPin.db", null, 1);
        bindItemsAndOps();

        doRegisterReceiver();
        Intent bindIntent = new Intent(this.getActivity(), RecMsgService.class);
        getActivity().bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(grpMsgReceiverBrief);
        getActivity().unregisterReceiver(grpQuitReceiver);
        getActivity().unregisterReceiver(grpIdLoadedReceiver);

        getActivity().unbindService(connection);
    }

    private void bindItemsAndOps() {
        listView = getActivity().findViewById(R.id.grp_msg_list);

        ConstraintLayout sysMsgClick = getActivity().findViewById(R.id.sys_msg_click);
        sysMsgClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toIntent = new Intent(v.getContext(), SysMsgCaseActivity.class);
                startActivity(toIntent);
            }
        });

        swipeRefreshLayout = getActivity().findViewById(R.id.grp_msg_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onRefresh() {
                @SuppressLint("HandlerLeak")
                final Handler refreshHandler = new Handler() {
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        if (msg.what == SUCCESS) {
                            for (Map.Entry<String, ArrayList<GrpMsgContent>> entry : allGrpsMsgListMap.entrySet()) {
                                ArrayList<GrpMsgContent> msgList = entry.getValue();
                                for (GrpMsgContent msgContent : msgList) {
                                    insertToDB(msgContent);
                                }
                            }
                            for (String grpId : grpIdList) {
                                queryFromDB(grpId);
                            }
                        } else {
                            Toast.makeText(getActivity(), "第" + msg.arg1 + "号拼单已被创建者删除！", Toast.LENGTH_LONG).show();
                            briefGrpMsgMap.remove(String.valueOf(msg.arg1));
                        }

                        // Show new result
                        showItems();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                };

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Get new from db and server
                        Message message = new Message();
                        for (final String grpId : grpIdList) {
                            try {
                                message.what = postQueryMsgs(grpId, true);
                                message.arg1 = Integer.parseInt(grpId);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        refreshHandler.sendMessage(message);
                    }
                }).start();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private int checkOfflineMsg(boolean isOffLine) {
        int res = SUCCESS;
        for (final String grpId : grpIdList) {
            try {
                int state = postQueryMsgs(grpId, isOffLine);
                if (state != SUCCESS) {
                    res = ERROR;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private int postQueryMsgs(String grpId, boolean isOffLine) throws IOException, JSONException {
        final String TAG = "QueryMsgs";
        int ret = 0;

        String url;
        if (isOffLine) {
            url = getOfflineMessageUrl(grpId);
        } else {
            url = getQueryChatMessageUrl(grpId);
        }

        Request request = new Request.Builder()
                .url(url)
                .header("token", idContent + "_" + tokenContent)
                .post(RequestBody.create(null, ""))
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            JSONObject responseJSON = new JSONObject(Objects.requireNonNull(response.body()).string());
            String codeString = responseJSON.getString("code");
            int code = Integer.parseInt(codeString);
            if (code == 100) {
                ArrayList<GrpMsgContent> allGrpsMsgList = new Gson().fromJson(responseJSON.getString("content"), new TypeToken<List<GrpMsgContent>>() {
                }.getType());
                if (allGrpsMsgList != null) {
                    Log.e(TAG, "msgList from " + grpId + " isOffLineMsg: " + isOffLine + ", grpMsgContentListFromPost isEmpty: " + allGrpsMsgList.isEmpty());
                    for (GrpMsgContent msgContent : allGrpsMsgList) {
                        long oldTime = Long.parseLong(msgContent.getTime());
                        String newTime = Instant.ofEpochMilli(oldTime).atZone(ZoneOffset.ofHours(8)).toLocalDateTime().toString().replace('T', ' ');
                        msgContent.setTime(newTime);
                        Log.e(TAG, msgContent.getTime());
                    }
                    allGrpsMsgListMap.put(grpId, allGrpsMsgList);
                    ret = SUCCESS;
                }
            }
        } else {
            ret = ERROR;
        }
        return ret;
    }

    private void showItems() {
        final String TAG = "showItems";
        final ArrayList<HashMap<String, String>> cases = new ArrayList<>();
        for (Map.Entry<String, BriefGrpMsg> entry : briefGrpMsgMap.entrySet()) {
            BriefGrpMsg briefGrpMsg = entry.getValue();
            HashMap<String, String> map = new HashMap<>();
            map.put("grpId", entry.getKey());
            map.put("grpTitle", briefGrpMsg.getGrpTitle());
            map.put("msgOwnerNickname", briefGrpMsg.getGrpMsgOwnNickname());
            map.put("grpMsgTime", briefGrpMsg.getGrpMsgTime());
            map.put("grpMsgContent", briefGrpMsg.getGrpMsgContent());
            cases.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(getActivity(), cases, R.layout.group_message_tip,
                new String[]{"grpMsgTime", "msgOwnerNickname", "grpMsgContent", "grpTitle"},
                new int[]{R.id.grp_msg_time, R.id.grp_msg_nickname, R.id.grp_msg_content, R.id.grp_title});
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent toIntent = new Intent(getActivity(), OneGrpMsgCaseActivity.class);
                toIntent.putExtra("grpId", cases.get((int) id).get("grpId"));
                toIntent.putExtra("grpTitle", cases.get((int) id).get("grpTitle"));
                startActivityForResult(toIntent, 1);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            showItems();
        }
    }

    private void queryFromDB(String grpId) {
        final String TAG = "queryFromDB";
        String[] args = new String[]{grpId};
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        Cursor cursor = db.query("GrpMsg",
                new String[]{"groupTitle", "nickname", "content", "time", "ownerId"},
                "groupId=?", args, null, null, "time desc", "1");
        if (cursor.moveToFirst()) {
            do {
                String ownerId = cursor.getString(cursor.getColumnIndex("ownerId"));
                Log.e(TAG, "queryFromDB: ownerId = " + ownerId);
                if (ownerId.equals(idContent)) {
                    String groupTitle = cursor.getString(cursor.getColumnIndex("groupTitle"));
                    String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
                    String content = cursor.getString(cursor.getColumnIndex("content"));
                    String time = cursor.getString(cursor.getColumnIndex("time"));
                    BriefGrpMsg briefGrpMsg = new BriefGrpMsg(groupTitle, nickname, content, time);
                    Log.e(TAG, "queryFromDB: grpId = " + grpId);
                    briefGrpMsgMap.put(grpId, briefGrpMsg);
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
        values.put("time", msgContent.getTime());
        long position = db.insertWithOnConflict("GrpMsg", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        Log.e("insertToDB", "from " + msgContent.getBillTitle() + "'s message " + msgContent.getContent() + " has been inserted to " + position + "th");
    }

    private void cleanDB(String grpId) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        String[] args = new String[]{grpId};
        db.delete("GrpMsg", "groupId=?", args);
    }
}
