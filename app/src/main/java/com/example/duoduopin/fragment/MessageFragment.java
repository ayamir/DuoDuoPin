package com.example.duoduopin.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
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
import com.example.duoduopin.bean.OrderContent;
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
import static com.example.duoduopin.tool.Constants.getQueryUrlByUserId;
import static com.example.duoduopin.tool.Constants.getRealTimeString;

public class MessageFragment extends Fragment {
    private ListView listView;

    private List<OrderContent> orderContent;
    private List<GrpMsgContent> grpMsgContentListFromPost;
    private final ArrayList<String> grpIdList = new ArrayList<>();
    private final HashMap<String, BriefGrpMsg> briefGrpMsgMap = new HashMap<>();

    private MyDBHelper myDBHelper;

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

        try {
            int state = postQueryOrdersByUserId();
            if (state == 1) {
                for (OrderContent content : orderContent) {
                    String grpId = content.getBillId();
                    if (!grpIdList.contains(grpId)) {
                        grpIdList.add(grpId);
                    }
                }
            } else {
                Toast.makeText(getActivity(), "获取群组聊天信息失败，请稍候再试！", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        final SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.grp_msg_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkOfflineMsg();
                showItems();
                swipeRefreshLayout.setRefreshing(false);
                insertToDB();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void checkOfflineMsg() {
        for (final String grpId : grpIdList) {
            try {
                int state = postQueryOfflineMsg(getOfflineMessageUrl(grpId));
                if (state != 1) {
                    Toast.makeText(getActivity(), "获取离线消息失败！", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showItems() {
        final String TAG = "showItems";
        final ArrayList<HashMap<String, String>> cases = new ArrayList<>();
        for (Map.Entry<String, BriefGrpMsg> entry : briefGrpMsgMap.entrySet()) {
            BriefGrpMsg briefGrpMsg = entry.getValue();
            HashMap<String, String> map = new HashMap<>();
            map.put("grpTitle", briefGrpMsg.getGrpTitle());
            map.put("grpMsgTime", briefGrpMsg.getGrpMsgTime());
            map.put("grpMsgContent", briefGrpMsg.getGrpMsgContent());
            cases.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(getActivity(), cases, R.layout.group_message_tip,
                new String[]{"grpMsgTime", "grpMsgContent", "grpTitle"},
                new int[]{R.id.grp_msg_time, R.id.grp_msg_content, R.id.grp_title});
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
        Cursor cursor = db.query("GrpNewMsg", new String[]{"groupOwnerId", "groupTitle", "content", "time"}, null, null, null, null, "time", null);
        if (cursor.moveToFirst()) {
            do {
                String groupOwnerId = cursor.getString(cursor.getColumnIndex("groupOwnerId"));
                int start = groupOwnerId.indexOf("_");
                String idFromDB = groupOwnerId.substring(start + 1);
                if (idFromDB.equals(idContent)) {
                    String groupId = groupOwnerId.substring(0, start);
                    String groupTitle = cursor.getString(cursor.getColumnIndex("groupTitle"));
                    String content = cursor.getString(cursor.getColumnIndex("content"));
                    String time = cursor.getString(cursor.getColumnIndex("time"));
                    BriefGrpMsg briefGrpMsg = new BriefGrpMsg(groupTitle, content, time);
                    briefGrpMsgMap.put(groupId, briefGrpMsg);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int postQueryOfflineMsg(String url) throws IOException, JSONException {
        final String TAG = "QueryOfflineMsg";
        int ret = 0;

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
                    for (GrpMsgContent grpMsgContent : grpMsgContentListFromPost) {
                        BriefGrpMsg briefGrpMsg = new BriefGrpMsg(grpMsgContent.getBillTitle(), grpMsgContent.getContent(), getRealTimeString(grpMsgContent.getTime()));
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

    private void insertToDB() {
        for (GrpMsgContent content : grpMsgContentListFromPost) {
            SQLiteDatabase db = myDBHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("groupOwnerId", content.getBillId() + "_" + idContent);
            values.put("groupTitle", content.getBillTitle());
            values.put("time", getRealTimeString(content.getTime()));
            values.put("content", content.getContent());
            db.replace("GrpNewMsg", null, values);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int postQueryOrdersByUserId() throws IOException, JSONException {
        final String TAG = "queryOrdersByUserId";

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
}
