package com.example.duoduopin.activity.message;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duoduopin.R;
import com.example.duoduopin.adapter.CreditAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.activity.MainActivity.client;
import static com.example.duoduopin.activity.MainActivity.idContent;
import static com.example.duoduopin.activity.MainActivity.tokenContent;
import static com.example.duoduopin.handler.GeneralMsgHandler.ERROR;
import static com.example.duoduopin.handler.GeneralMsgHandler.SUCCESS;
import static com.example.duoduopin.tool.Constants.creditUrl;

public class SysMsgCreditActivity extends AppCompatActivity {
    private String groupName;
    private ArrayList<String> nicknameList;
    private ArrayList<String> userIdList;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sys_msg_credit);

        getDataFromIntent();

        ImageView ivCreditBack = findViewById(R.id.iv_credit_back);
        ivCreditBack.setOnClickListener(v -> finish());

        TextView tvCreditGroupName = findViewById(R.id.tv_credit_group_name);
        tvCreditGroupName.setText(groupName);

        RecyclerView rvCredit = findViewById(R.id.rv_credit);
        CreditAdapter adapter = new CreditAdapter(nicknameList, userIdList);
        rvCredit.setAdapter(adapter);

        Button btnSubmitCredit = findViewById(R.id.btn_submit_credit);
        btnSubmitCredit.setOnClickListener(v -> {
            int creditedNum = adapter.getItemCount();
            if (creditedNum != nicknameList.size()) {
                Toast.makeText(v.getContext(), "请完成对所有用户的评分之后再提交哦～", Toast.LENGTH_SHORT).show();
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

    private void getDataFromIntent() {
        Intent fromIntent = getIntent();
        if (fromIntent != null) {
            groupName = fromIntent.getStringExtra("groupName");
            nicknameList = fromIntent.getStringArrayListExtra("nicknameList");
            userIdList = fromIntent.getStringArrayListExtra("userIdList");
        }
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
