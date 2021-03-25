package com.example.duoduopin.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.duoduopin.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.activity.LoginActivity.idContent;
import static com.example.duoduopin.activity.LoginActivity.tokenContent;
import static com.example.duoduopin.activity.MainActivity.client;
import static com.example.duoduopin.tool.Constants.getAllowUrl;
import static com.example.duoduopin.tool.Constants.getQueryUrlByOrderId;
import static com.example.duoduopin.tool.Constants.getQueryUserUrl;
import static com.example.duoduopin.tool.Constants.getRealTimeString;
import static com.example.duoduopin.tool.Constants.getRejectUrl;

public class OneSysMsgCaseActivity extends AppCompatActivity {
    private String messageIdString;
    private String senderIdString;
    private String billIdString;
    private String messageTypeString;
    private String timeString;
    private String contentString;
    private String isFromServerString;
    private JSONObject orderContentJSON;

    private String nickname;
    private ImageView backButton;
    private TextView senderNickname;
    private TextView contentView;
    private TextView timeView;
    private LinearLayout checkDetailsLayout;
    private Button agree;
    private Button reject;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_sysmsg_case);

        getInfoFromIntent();
        bindViews();
        initialize();
    }

    private void getInfoFromIntent() {
        Intent fromIntent = getIntent();
        if (fromIntent != null) {
            messageIdString = fromIntent.getStringExtra("messageId");
            senderIdString = fromIntent.getStringExtra("senderId");
            billIdString = fromIntent.getStringExtra("billId");
            messageTypeString = fromIntent.getStringExtra("messageType");
            timeString = fromIntent.getStringExtra("time");
            contentString = fromIntent.getStringExtra("content");
            isFromServerString = fromIntent.getStringExtra("isFromServer");
        }
    }

    private void bindViews() {
        backButton = findViewById(R.id.back_button);
        senderNickname = findViewById(R.id.sender_nickname);
        contentView = findViewById(R.id.content_view);
        timeView = findViewById(R.id.time_view);
        checkDetailsLayout = findViewById(R.id.check_details_layout);
        agree = findViewById(R.id.agree_button);
        reject = findViewById(R.id.reject_button);

        if (!messageTypeString.equals("APPLY") || isFromServerString.equals("false")) {
            Log.d("bindViews", "bindViews: messageTypeString = " + messageTypeString);
            agree.setVisibility(View.INVISIBLE);
            reject.setVisibility(View.INVISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initialize() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        try {
            int state = postSearchUser(getQueryUserUrl(senderIdString));
            if (state == 1) {
                senderNickname.setText(nickname);
            } else {
                Toast.makeText(this, "获取用户昵称出现异常！", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        contentView.setText(contentString);
        timeView.setText(timeString);
        checkDetailsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int state = postSearchOrder(getQueryUrlByOrderId(billIdString));
                    if (state == 1) {
                        Intent toIntent = new Intent(v.getContext(), OneOrderCaseActivity.class);
                        toIntent.putExtra("orderId", orderContentJSON.getString("billId"));
                        toIntent.putExtra("userId", orderContentJSON.getString("userId"));
                        toIntent.putExtra("nickname", orderContentJSON.getString("nickname"));
                        toIntent.putExtra("type", orderContentJSON.getString("type"));
                        toIntent.putExtra("price", orderContentJSON.getString("price"));
                        toIntent.putExtra("address", orderContentJSON.getString("address"));
                        toIntent.putExtra("curPeople", orderContentJSON.getString("curPeople"));
                        toIntent.putExtra("maxPeople", orderContentJSON.getString("maxPeople"));
                        toIntent.putExtra("time", getRealTimeString(orderContentJSON.getString("time")));
                        toIntent.putExtra("description", orderContentJSON.getString("description"));
                        toIntent.putExtra("title", orderContentJSON.getString("title"));
                        startActivity(toIntent);
                    } else {
                        Toast.makeText(v.getContext(), "获取拼单详情失败，请稍候再试！", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String allowUrl = getAllowUrl(messageIdString);
                    int state = postAllowOrReject(allowUrl);
                    if (state == 1) {
                        Toast.makeText(v.getContext(), "请求已通过！", Toast.LENGTH_SHORT).show();
                        agree.setVisibility(View.INVISIBLE);
                        reject.setVisibility(View.INVISIBLE);
                    } else if (state == -1) {
                        Toast.makeText(v.getContext(), "请检查网络状况稍后再试！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(v.getContext(), "遇到未知错误，请稍后再试！", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String rejectUrl = getRejectUrl(messageIdString);
                    int state = postAllowOrReject(rejectUrl);
                    if (state == 1) {
                        Toast.makeText(v.getContext(), "请求已拒绝！", Toast.LENGTH_SHORT).show();
                        agree.setVisibility(View.INVISIBLE);
                        reject.setVisibility(View.INVISIBLE);
                    } else if (state == -1) {
                        Toast.makeText(v.getContext(), "请检查网络状况稍后再试！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(v.getContext(), "遇到未知错误，请稍候再试！", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int postSearchOrder(String url) throws IOException, JSONException {
        int ret = 0;

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
                JSONArray orderContentArray = new JSONArray(responseJSON.getString("content"));
                orderContentJSON = orderContentArray.getJSONObject(0);
                ret = 1;
            }
        } else {
            ret = -1;
        }

        return ret;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int postSearchUser(String url) throws IOException, JSONException {
        final String TAG = "postSearchUser";
        int ret = 0;

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
                JSONObject contentJSON = new JSONObject(responseJSON.getString("content"));
                Log.d(TAG, "postSearchUser: contentJSON = " + contentJSON.toString());
                nickname = contentJSON.getString("nickname");
                ret = 1;
            } else {
                ret = 2;
            }
        } else {
            ret = -1;
        }

        return ret;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int postAllowOrReject(String url) throws IOException, JSONException {
        final String TAG = "postAllowJoin";
        int ret = 0;

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
                ret = 1;
            } else {
                ret = 2;
                Log.d(TAG, "postAllowOrReject: responseJSON = " + responseJSON.toString());
            }
        } else {
            ret = -1;
        }

        return ret;
    }
}
