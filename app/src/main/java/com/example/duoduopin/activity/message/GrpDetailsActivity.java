package com.example.duoduopin.activity.message;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duoduopin.R;
import com.example.duoduopin.adapter.BriefMemberInfoAdapter;
import com.example.duoduopin.pojo.BriefMemberInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.activity.MainActivity.client;
import static com.example.duoduopin.activity.MainActivity.idContent;
import static com.example.duoduopin.activity.MainActivity.tokenContent;
import static com.example.duoduopin.activity.order.OneOrderCaseActivity.getDownloadPath;
import static com.example.duoduopin.handler.GeneralMsgHandler.ERROR;
import static com.example.duoduopin.handler.GeneralMsgHandler.FAILED;
import static com.example.duoduopin.handler.GeneralMsgHandler.SUCCESS;
import static com.example.duoduopin.tool.Constants.getQueryMemberUrl;
import static com.example.duoduopin.tool.Constants.getQuitUrl;
import static com.example.duoduopin.tool.Constants.group_quit_signal;

public class GrpDetailsActivity extends AppCompatActivity {
    private String orderIdString;
    private String groupTitle;
    private final ArrayList<BriefMemberInfo> memberInfoList = new ArrayList<>();

    private RecyclerView rvGroupMember;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grp_details);

        getInfoFromIntent();

        TextView tvGroupTitle = findViewById(R.id.tv_group_title);
        tvGroupTitle.setText(groupTitle);

        rvGroupMember = findViewById(R.id.rv_group_member);
        rvGroupMember.setLayoutManager(new LinearLayoutManager(this));

        DialogInterface.OnClickListener quitClickListener = ((dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    @SuppressLint("HandlerLeak") final Handler quitHandler = new Handler() {
                        @Override
                        public void handleMessage(@NonNull Message msg) {
                            switch (msg.what) {
                                case SUCCESS:
                                    Toast.makeText(GrpDetailsActivity.this, "您已成功退出该小组", Toast.LENGTH_SHORT).show();

                                    // To close websocket, remove tip
                                    Intent quitIntent = new Intent();
                                    quitIntent.putExtra("quitGrpId", orderIdString);
                                    Log.e("quitOrder", "quitGrpId =" + orderIdString);
                                    quitIntent.setAction(group_quit_signal);
                                    sendBroadcast(quitIntent);
                                    break;
                                case ERROR:
                                    Toast.makeText(GrpDetailsActivity.this, "遇到未知错误，请稍后再试", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(GrpDetailsActivity.this, "退出小组失败，请稍后再试", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    };
                    new Thread(() -> {
                        try {
                            Message message = new Message();
                            message.what = delQuitOrder(getQuitUrl(orderIdString, idContent));
                            quitHandler.sendMessage(message);
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }).start();

                    setResult(RESULT_OK, null);
                    finish();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
            }
        });

        final AlertDialog.Builder groupQuitBuider = new AlertDialog.Builder(this);
        groupQuitBuider.setTitle("确认");
        groupQuitBuider.setMessage("确定退出吗？")
                .setPositiveButton("确定", quitClickListener)
                .setNegativeButton("我再想想", quitClickListener);

        Button btnGroupQuit = findViewById(R.id.btn_group_quit);
        btnGroupQuit.setOnClickListener(v -> groupQuitBuider.show());
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getInfoFromIntent() {
        Intent fromIntent = getIntent();
        if (fromIntent != null) {
            orderIdString = fromIntent.getStringExtra("orderId");
            groupTitle = fromIntent.getStringExtra("groupTitle");

            @SuppressLint("HandlerLeak") final Handler isInHandler = new Handler() {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == SUCCESS) {
                        BriefMemberInfoAdapter adapter = new BriefMemberInfoAdapter(memberInfoList);
                        rvGroupMember.setAdapter(adapter);
                    }
                }
            };

            new Thread(() -> {
                Message message = new Message();
                try {
                    message.what = postQueryGrpMem(getQueryMemberUrl(orderIdString));
                    isInHandler.sendMessage(message);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int postQueryGrpMem(String url) throws IOException, JSONException {
        final String TAG = "postQueryGrpMem";
        int ret;

        final Request request = new Request.Builder()
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
                ret = SUCCESS;
                JSONArray contentArray = new JSONArray(responseJSON.getString("content"));
                for (int i = 0; i < contentArray.length(); i++) {
                    String userId = contentArray.getJSONObject(i).getString("userId");
                    String nickname = contentArray.getJSONObject(i).getString("nickname");
                    String memberHeadUrl = contentArray.getJSONObject(i).getString("url");
                    String credit = contentArray.getJSONObject(i).getString("point");
                    String path = getDownloadPath(memberHeadUrl, userId);
                    BriefMemberInfo briefMemberInfo = new BriefMemberInfo(nickname, credit, userId, path);
                    Log.e(TAG, briefMemberInfo.toString());

                    memberInfoList.add(briefMemberInfo);
                }
            } else {
                ret = FAILED;
            }
        } else {
            ret = ERROR;
        }
        return ret;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int delQuitOrder(String url) throws IOException, JSONException {
        int ret;

        final Request request = new Request.Builder()
                .url(url)
                .header("token", idContent + "_" + tokenContent)
                .delete()
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            JSONObject responseJSON = new JSONObject(Objects.requireNonNull(response.body()).string());
            String codeString = responseJSON.getString("code");
            int code = Integer.parseInt(codeString);
            if (code == 100) {
                ret = 1;
            } else {
                ret = 2;
            }
        } else {
            ret = -1;
        }
        return ret;
    }
}
