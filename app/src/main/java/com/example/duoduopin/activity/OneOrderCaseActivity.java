package com.example.duoduopin.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.activity.LoginActivity.idContent;
import static com.example.duoduopin.activity.LoginActivity.tokenContent;
import static com.example.duoduopin.tool.Constants.getDelOrderUrl;
import static com.example.duoduopin.tool.Constants.getJoinUrl;
import static com.example.duoduopin.tool.Constants.getQueryMemberUrl;
import static com.example.duoduopin.tool.Constants.getQuitUrl;

public class OneOrderCaseActivity extends AppCompatActivity {

    private String userIdString, nicknameString, orderIdString, typeString, priceString, addressString, curPeopleString, maxPeopleString, timeString, descriptionString, titleString;
    private ArrayList<String> members = new ArrayList<>();

    private Button delete, join, leave;
    private ImageView back;

    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_order_case);
        getInfoFromIntent();
        initValue();
        bindBtn();
        bindOperation();
        setVisibility();
    }

    private void setVisibility() {
        if (userIdString.equals(idContent)) {
            delete.setVisibility(View.VISIBLE);
            leave.setVisibility(View.INVISIBLE);
            join.setVisibility(View.INVISIBLE);
        } else {
            delete.setVisibility(View.INVISIBLE);
        }

        try {
            int state = postQueryGrpMem(getQueryMemberUrl(orderIdString));
            if (state == 1) {
                if (isInMembers()) {
                    leave.setVisibility(View.VISIBLE);
                    join.setVisibility(View.INVISIBLE);
                } else {
                    leave.setVisibility(View.INVISIBLE);
                    join.setVisibility(View.VISIBLE);
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isInMembers() {
        return members.contains(idContent);
    }

    private void getInfoFromIntent() {
        Intent fromIntent = getIntent();
        if (fromIntent != null) {
            orderIdString = fromIntent.getStringExtra("orderId");
            if (orderIdString != null) {
                userIdString = fromIntent.getStringExtra("userId");
                nicknameString = fromIntent.getStringExtra("nickname");
                typeString = fromIntent.getStringExtra("type");
                priceString = fromIntent.getStringExtra("price");
                addressString = fromIntent.getStringExtra("address");
                curPeopleString = fromIntent.getStringExtra("curPeople");
                maxPeopleString = fromIntent.getStringExtra("maxPeople");
                timeString = fromIntent.getStringExtra("time").replace('T', ' ');
                descriptionString = fromIntent.getStringExtra("description");
                titleString = fromIntent.getStringExtra("title");
            }
        }
    }

    private void bindBtn() {
        join = findViewById(R.id.joinButton);
        leave = findViewById(R.id.leaveButton);
        delete = findViewById(R.id.deleteButton);
        back = findViewById(R.id.backButton);
    }

    private void bindOperation() {
        join.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                try {
                    int state = putJoin(getJoinUrl(orderIdString));
                    if (state == 1) {
                        Toast.makeText(v.getContext(), "请求发送成功！", Toast.LENGTH_SHORT).show();
                    } else if (state == 2) {
                        Toast.makeText(v.getContext(), "您已发送过请求，无需重复发送！", Toast.LENGTH_SHORT).show();
                    } else if (state == 3) {
                        Toast.makeText(v.getContext(), "遇到未知错误，请稍后再试", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(v.getContext(), "请检查网络状况稍后再试", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        DialogInterface.OnClickListener quitClickListener = new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        try {
                            int state = delQuitOrder(getQuitUrl(orderIdString, idContent));
                            if (state == 1) {
                                Toast.makeText(OneOrderCaseActivity.this, "您已成功退出该小组", Toast.LENGTH_SHORT).show();
                            } else if (state == 2) {
                                Toast.makeText(OneOrderCaseActivity.this, "退出小组失败，请稍后再试", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(OneOrderCaseActivity.this, "遇到未知错误，请稍后再试", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        final AlertDialog.Builder quitBuilder = new AlertDialog.Builder(this);
        quitBuilder.setTitle("确认");
        quitBuilder.setMessage("确定离开吗？")
                .setPositiveButton("确定", quitClickListener)
                .setNegativeButton("我再想想", quitClickListener);

        leave.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                quitBuilder.show();
            }
        });

        DialogInterface.OnClickListener deleteClickListener = new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        try {
                            int state = delDisbandOrder(getDelOrderUrl(orderIdString));
                            if (state == 1) {
                                // finish之后应该返回上上层activity
                                finish();
                                Toast.makeText(OneOrderCaseActivity.this, "解散成功！", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(OneOrderCaseActivity.this, "解散失败！", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        final AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(this);
        deleteBuilder.setTitle("确认");
        deleteBuilder.setMessage("确定解散吗？")
                .setPositiveButton("确定", deleteClickListener)
                .setNegativeButton("我再想想", deleteClickListener);

        delete.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                deleteBuilder.show();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private int postQueryGrpMem(String url) throws IOException, JSONException {
        final String TAG = "postQueryGrpMem";
        int ret = 1;

        final Request request = new Request.Builder()
                .url(url)
                .header("token", idContent + "_" + tokenContent)
                .post(RequestBody.create(null, ""))
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            JSONObject responseJSON = new JSONObject(response.body().string());
            String codeString = responseJSON.getString("code");
            int code = Integer.parseInt(codeString);
            if (code == 100) {
                ret = 1;
                JSONArray contentArray = new JSONArray(responseJSON.getString("content"));
                if (contentArray != null) {
                    for (int i = 0; i < contentArray.length(); i++) {
                        members.add(contentArray.getJSONObject(i).getString("userId"));
                    }
                }
            } else {
                ret = 2;
            }
        } else {
            ret = -1;
        }

        return ret;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int delQuitOrder(String url) throws IOException, JSONException {
        final String TAG = "delQuitOrder";
        int ret = 0;

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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int putJoin(String url) throws IOException, JSONException {
        final String TAG = "putJoin";
        int ret;

        final Request request = new Request.Builder()
                .url(url)
                .header("token", idContent + "_" + tokenContent)
                .put(RequestBody.create(null, ""))
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        JSONObject responseJSON = new JSONObject(Objects.requireNonNull(response.body()).string());
        String codeString = responseJSON.getString("code");
        int code = Integer.parseInt(codeString);

        if (code == 100) {
            ret = 1;
        } else if (code == -1009) {
            ret = 2;
        } else {
            ret = 3;
            Log.d(TAG, "responseContent: " + responseJSON.toString());
        }

        return ret;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int delDisbandOrder(String url) throws IOException {
        final String TAG = "delOrder";
        int ret = 0;

        final Request request = new Request.Builder()
                .url(url)
                .header("token", idContent + "_" + tokenContent)
                .delete()
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            ret = 1;
        } else {
            Log.d(TAG, Objects.requireNonNull(response.body()).string());
            Log.d(TAG, response.toString());
        }

        return ret;
    }

    private void initValue() {
        EditText nickname = findViewById(R.id.nickname);
        EditText userId = findViewById(R.id.userId);
        EditText orderId = findViewById(R.id.orderId);
        EditText type = findViewById(R.id.type);
        EditText price = findViewById(R.id.price);
        EditText address = findViewById(R.id.address);
        EditText curPeople = findViewById(R.id.curNumber);
        EditText maxPeople = findViewById(R.id.maxNumber);
        TextView time = findViewById(R.id.time);
        TextView description = findViewById(R.id.description);
        TextView title = findViewById(R.id.title);

        nickname.setText(nicknameString);
        userId.setText(userIdString);
        type.setText(typeString);
        orderId.setText(orderIdString);
        price.setText(priceString);
        address.setText(addressString);
        curPeople.setText(curPeopleString);
        maxPeople.setText(maxPeopleString);
        time.setText(timeString);
        description.setText(descriptionString);
        title.setText(titleString);
    }
}
