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
import android.os.IBinder;
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
import com.example.duoduopin.service.RecGrpMsgService;
import com.example.duoduopin.tool.MyDBHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.activity.LoginActivity.idContent;
import static com.example.duoduopin.activity.LoginActivity.tokenContent;
import static com.example.duoduopin.activity.MainActivity.client;
import static com.example.duoduopin.activity.MainActivity.isMessageClicked;
import static com.example.duoduopin.tool.Constants.getOfflineMessageUrl;
import static com.example.duoduopin.tool.Constants.getQueryChatMessageUrl;
import static com.example.duoduopin.tool.Constants.getRealTimeString;

public class MessageFragment extends Fragment {
    private ListView listView;

    private ArrayList<String> grpIdList;
    private List<GrpMsgContent> grpMsgContentListFromPost;
    private final HashMap<String, BriefGrpMsg> briefGrpMsgMap = new HashMap<>();

    private MyDBHelper myDBHelper;

    private final ServiceConnection connection = new ServiceConnection() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            Log.e("MessageFragment", "onServiceConnected");
            RecGrpMsgService.RecGrpMsgBinder binder = (RecGrpMsgService.RecGrpMsgBinder) iBinder;
            RecGrpMsgService recGrpMsgService = binder.getService();
            grpIdList = recGrpMsgService.getGrpIdList();
            checkOfflineMsg(false);
            insertToDB(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("MessageFragment", "onServiceDisconnected");
        }
    };

    private class GrpMsgReceiverBrief extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            BriefGrpMsg briefGrpMsg = (BriefGrpMsg) intent.getSerializableExtra("briefGrpMsg");
            String grpId = intent.getStringExtra("grpId");
            Log.e("Receiver", "onReceive: grpId = " + grpId);
            briefGrpMsgMap.put(grpId, briefGrpMsg);
            showItems();
        }
    }

    private void doRegisterReceiver() {
        GrpMsgReceiverBrief grpMsgReceiverBrief = new GrpMsgReceiverBrief();
        IntentFilter intentFilter = new IntentFilter("com.example.duoduopin.grpmsg.brief");
        getActivity().registerReceiver(grpMsgReceiverBrief, intentFilter);
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        myDBHelper = new MyDBHelper(getActivity(), "DuoDuoPin.db", null, 1);
        listView = getActivity().findViewById(R.id.grp_msg_list);

        Intent bindIntent = new Intent(this.getActivity(), RecGrpMsgService.class);
        getActivity().bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
        doRegisterReceiver();

        if (isMessageClicked) {
            queryFromDB();
            showItems();
        }

        ConstraintLayout sysMsgClick = getActivity().findViewById(R.id.sys_msg_click);
        sysMsgClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toIntent = new Intent(v.getContext(), SysMsgCaseActivity.class);
                startActivity(toIntent);
            }
        });

        final SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.grp_msg_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // clear old instances
                briefGrpMsgMap.clear();

                // Get new from db and server
                queryFromDB();
                checkOfflineMsg(true);

                // Show new result
                showItems();

                swipeRefreshLayout.setRefreshing(false);
                insertToDB(true);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void checkOfflineMsg(boolean isOffLine) {
        for (final String grpId : grpIdList) {
            try {
                int state = postQueryMsgs(grpId, isOffLine);
                if (state != 1) {
                    Toast.makeText(getActivity(), "获取离线消息失败！", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
                grpMsgContentListFromPost = new Gson().fromJson(responseJSON.getString("content"), new TypeToken<List<GrpMsgContent>>() {
                }.getType());
                if (grpMsgContentListFromPost != null) {
                    Log.e(TAG, "isOffLineMsg: " + isOffLine + ", grpMsgContentListFromPost isEmpty: " + grpMsgContentListFromPost.isEmpty());
                    for (GrpMsgContent grpMsgContent : grpMsgContentListFromPost) {
                        BriefGrpMsg briefGrpMsg = new BriefGrpMsg(grpMsgContent.getBillTitle(), grpMsgContent.getNickname(), grpMsgContent.getContent(), getRealTimeString(grpMsgContent.getTime()));
                        briefGrpMsgMap.put(grpMsgContent.getBillId(), briefGrpMsg);
                    }
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
                startActivity(toIntent);
            }
        });
    }

    private void queryFromDB() {
        final String TAG = "queryFromDB";
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        Cursor cursor = db.query("GrpNewMsg", new String[]{"groupOwnerId", "groupTitle", "msgOwnerNickname", "content", "time"}, null, null, null, null, "time", null);
        if (cursor.moveToFirst()) {
            do {
                String groupOwnerId = cursor.getString(cursor.getColumnIndex("groupOwnerId"));
                int start = groupOwnerId.indexOf("_");
                String idFromDB = groupOwnerId.substring(start + 1);
                if (idFromDB.equals(idContent)) {
                    String groupId = groupOwnerId.substring(0, start);
                    String groupTitle = cursor.getString(cursor.getColumnIndex("groupTitle"));
                    String msgOwnerNickname = cursor.getString(cursor.getColumnIndex("msgOwnerNickname"));
                    String content = cursor.getString(cursor.getColumnIndex("content"));
                    String time = cursor.getString(cursor.getColumnIndex("time"));
                    BriefGrpMsg briefGrpMsg = new BriefGrpMsg(groupTitle, msgOwnerNickname,content, time);
                    Log.e(TAG, "queryFromDB: grpId = " + groupId);
                    briefGrpMsgMap.put(groupId, briefGrpMsg);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void insertToDB(boolean isOffLine) {
        if (null != grpMsgContentListFromPost) {
            for (GrpMsgContent content : grpMsgContentListFromPost) {
                SQLiteDatabase db = myDBHelper.getWritableDatabase();
                ContentValues valuesToNewMsg = new ContentValues();
                valuesToNewMsg.put("groupOwnerId", content.getBillId() + "_" + idContent);
                valuesToNewMsg.put("groupTitle", content.getBillTitle());
                valuesToNewMsg.put("msgOwnerNickname", content.getNickname());
                valuesToNewMsg.put("time", getRealTimeString(content.getTime()));
                valuesToNewMsg.put("content", content.getContent());
                db.replace("GrpNewMsg", null, valuesToNewMsg);

                // Get all messages for the first time
                if (!isOffLine) {
                    ContentValues valuesToFullMsg = new ContentValues();
                    valuesToFullMsg.put("groupId", Integer.valueOf(content.getBillId()));
                    valuesToFullMsg.put("groupTitle", content.getBillTitle());
                    valuesToFullMsg.put("ownerId", Integer.valueOf(idContent));
                    valuesToFullMsg.put("userId", Integer.valueOf(content.getUserId()));
                    valuesToFullMsg.put("nickname", content.getNickname());
                    valuesToFullMsg.put("content", content.getContent());
                    valuesToFullMsg.put("time", content.getTime());
                    db.insertWithOnConflict("GrpMsg", null, valuesToFullMsg, SQLiteDatabase.CONFLICT_IGNORE);
                }
            }
        }
    }
}
