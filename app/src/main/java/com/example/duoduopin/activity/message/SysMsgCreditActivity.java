package com.example.duoduopin.activity.message;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duoduopin.R;
import com.example.duoduopin.adapter.CreditAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.activity.MainActivity.client;
import static com.example.duoduopin.activity.MainActivity.idContent;
import static com.example.duoduopin.activity.MainActivity.tokenContent;
import static com.example.duoduopin.handler.GeneralMsgHandler.ERROR;
import static com.example.duoduopin.handler.GeneralMsgHandler.FAILED;
import static com.example.duoduopin.handler.GeneralMsgHandler.SUCCESS;
import static com.example.duoduopin.tool.Constants.creditUrl;
import static com.example.duoduopin.tool.Constants.getQueryMemberUrl;

public class SysMsgCreditActivity extends AppCompatActivity {
    private final ArrayList<String> nicknameList = new ArrayList<>();
    private final ArrayList<String> userIdList = new ArrayList<>();
    private String isRead;
    private String groupId;
    private RecyclerView rvCredit;
    private CreditAdapter adapter;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sys_msg_credit);

        getDataFromIntent();

        ImageView ivCreditBack = findViewById(R.id.iv_credit_back);
        ivCreditBack.setOnClickListener(v -> finish());

        TextView tvCreditGroupName = findViewById(R.id.tv_credit_group_name);
        tvCreditGroupName.setText(groupId);

        rvCredit = findViewById(R.id.rv_credit);

        Button btnSubmitCredit = findViewById(R.id.btn_submit_credit);
        if (isRead.equals("true")) {
            btnSubmitCredit.setVisibility(View.INVISIBLE);
        }
        btnSubmitCredit.setOnClickListener(v -> {
            int creditedNum = adapter.getItemCount();
            if (creditedNum != nicknameList.size()) {
                Toast.makeText(v.getContext(), "请完成对所有用户的评分之后再提交哦~", Toast.LENGTH_SHORT).show();
            } else {
                int res = 0;
                HashMap<String, Integer> creditMap = adapter.getCreditMap();
                for (String userId : userIdList) {
                    try {
                        res += postCredit(userId, String.valueOf(creditMap.get(userId)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (res == creditedNum) {
                    Toast.makeText(v.getContext(), "评分提交成功！", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getDataFromIntent() {
        Intent fromIntent = getIntent();
        if (fromIntent != null) {
            isRead = fromIntent.getStringExtra("isRead");
            groupId = fromIntent.getStringExtra("groupId");

            new Thread(new Runnable() {
                @SuppressLint("HandlerLeak")
                final Handler getUsersHandler = new Handler() {
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        if (msg.what == SUCCESS) {
                            adapter = new CreditAdapter(nicknameList, userIdList);
                            rvCredit.setAdapter(adapter);
                        }
                    }
                };

                @Override
                public void run() {
                    try {
                        Message message = new Message();
                        message.what = postQueryGrpMem(getQueryMemberUrl(groupId));
                        getUsersHandler.sendMessage(message);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
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
                    nicknameList.add(nickname);
                    userIdList.add(userId);
                }
            } else {
                ret = FAILED;
            }
        } else {
            ret = ERROR;
        }
        return ret;
    }

    private int postCredit(String userId, String credit) throws IOException {
        int res;
        final String url = creditUrl + userId + "/" + credit;

        final Request request = new Request.Builder()
                .url(url)
                .header("token", idContent + "_" + tokenContent)
                .post(RequestBody.create(null, ""))
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            res = SUCCESS;
        } else {
            res = ERROR;
        }

        return res;
    }
}
