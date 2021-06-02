package com.example.duoduopin.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.duoduopin.R;
import com.example.duoduopin.tool.MyDBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
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
import static com.example.duoduopin.tool.Constants.getAllowUrl;
import static com.example.duoduopin.tool.Constants.getQueryUrlByOrderId;
import static com.example.duoduopin.tool.Constants.getQueryUserUrl;
import static com.example.duoduopin.tool.Constants.getRejectUrl;

public class OneSysMsgCaseActivity extends AppCompatActivity {
    private String messageIdString;
    private String senderIdString;
    private String billIdString;
    private String messageTypeString;
    private String timeString;
    private String contentString;
    private String isRead;
    private JSONObject orderContentJSON;

    private String nickname;
    private ImageView backButton;
    private TextView senderNickname;
    private TextView contentView;
    private TextView timeView;
    private LinearLayout checkDetailsLayout;
    private Button agree;
    private Button reject;

    private final Context mContext = this;

    private final MyDBHelper myDBHelper = new MyDBHelper(this, "DuoDuoPin.db", null, 1);

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
            isRead = fromIntent.getStringExtra("isRead");
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

        if (!messageTypeString.equals("APPLY") || isRead.equals("true")) {
            Log.d("bindViews", "bindViews: messageTypeString = " + messageTypeString);
            agree.setVisibility(View.INVISIBLE);
            reject.setVisibility(View.INVISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initialize() {
        backButton.setOnClickListener(v -> finish());

        @SuppressLint("HandlerLeak") final Handler searchUserHandlers = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == SUCCESS) {
                    senderNickname.setText(nickname);
                } else {
                    Toast.makeText(mContext, "获取用户昵称出现异常！", Toast.LENGTH_SHORT).show();
                }
            }
        };
        new Thread(() -> {
            try {
                Message message = new Message();
                message.what = postSearchUser(getQueryUserUrl(senderIdString));
                searchUserHandlers.sendMessage(message);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();

        contentView.setText(contentString);
        timeView.setText(timeString);
        checkDetailsLayout.setOnClickListener(v -> {
            @SuppressLint("HandlerLeak") final Handler checkDetailsHandler = new Handler() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void handleMessage(@NonNull Message msg) {
                    if (msg.what == SUCCESS) {
                        Intent toIntent = new Intent(v.getContext(), OneOrderCaseActivity.class);
                        try {
                            toIntent.putExtra("orderId", orderContentJSON.getString("billId"));
                            toIntent.putExtra("userId", orderContentJSON.getString("userId"));
                            toIntent.putExtra("nickname", orderContentJSON.getString("nickname"));
                            toIntent.putExtra("type", orderContentJSON.getString("type"));
                            toIntent.putExtra("price", orderContentJSON.getString("price"));
                            toIntent.putExtra("address", orderContentJSON.getString("address"));
                            toIntent.putExtra("curPeople", orderContentJSON.getString("curPeople"));
                            toIntent.putExtra("maxPeople", orderContentJSON.getString("maxPeople"));
                            long oldTime = Long.parseLong(orderContentJSON.getString("time"));
                            String newTime = Instant.ofEpochMilli(oldTime).atZone(ZoneOffset.ofHours(8)).toLocalDateTime().toString().replace('T', ' ');
                            toIntent.putExtra("time", newTime);
                            toIntent.putExtra("description", orderContentJSON.getString("description"));
                            toIntent.putExtra("title", orderContentJSON.getString("title"));
                            startActivity(toIntent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(v.getContext(), "获取拼单详情失败，请稍候再试！", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            new Thread(() -> {
                try {
                    Message message = new Message();
                    message.what = postSearchOrder(getQueryUrlByOrderId(billIdString));
                    checkDetailsHandler.sendMessage(message);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }).start();
        });
        agree.setOnClickListener(v -> {
            @SuppressLint("HandlerLeak") final Handler allowHandler = new Handler() {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    if (msg.what == SUCCESS) {
                        Toast.makeText(v.getContext(), "请求已通过！", Toast.LENGTH_SHORT).show();
                        SQLiteDatabase db = myDBHelper.getWritableDatabase();
                        db.execSQL("update SysMsg set isRead=" + "'" + true + "'" + " where messageId=" + "'" + messageIdString + "'");
                        agree.setVisibility(View.INVISIBLE);
                        reject.setVisibility(View.INVISIBLE);
                    } else {
                        Toast.makeText(v.getContext(), "请检查网络状况稍后再试！", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            new Thread(() -> {
                try {
                    String allowUrl = getAllowUrl(messageIdString);
                    Message message = new Message();
                    message.what = postAllowOrReject(allowUrl);
                    allowHandler.sendMessage(message);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }).start();
        });
        reject.setOnClickListener(v -> {
            @SuppressLint("HandlerLeak") final Handler rejectHandler = new Handler() {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    if (msg.what == SUCCESS) {
                        Toast.makeText(v.getContext(), "请求已拒绝！", Toast.LENGTH_SHORT).show();
                        SQLiteDatabase db = myDBHelper.getWritableDatabase();
                        db.execSQL("update SysMsg set isRead=" + "'" + true + "'" + " where messageId=" + "'" + messageIdString + "'");
                        agree.setVisibility(View.INVISIBLE);
                        reject.setVisibility(View.INVISIBLE);
                    } else {
                        Toast.makeText(v.getContext(), "请检查网络状况稍后再试！", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            new Thread(() -> {
                try {
                    String rejectUrl = getRejectUrl(messageIdString);
                    Message message = new Message();
                    message.what = postAllowOrReject(rejectUrl);
                    rejectHandler.sendMessage(message);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }).start();

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
                ret = SUCCESS;
            }
        } else {
            ret = ERROR;
        }

        return ret;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int postAllowOrReject(String url) throws IOException, JSONException {
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
                ret = SUCCESS;
            }
        } else {
            ret = ERROR;
        }

        return ret;
    }
}
