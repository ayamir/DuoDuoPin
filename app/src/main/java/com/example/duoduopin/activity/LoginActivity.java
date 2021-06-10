package com.example.duoduopin.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.duoduopin.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.activity.MainActivity.idContent;
import static com.example.duoduopin.activity.MainActivity.nicknameContent;
import static com.example.duoduopin.activity.MainActivity.prefs;
import static com.example.duoduopin.activity.MainActivity.tokenContent;
import static com.example.duoduopin.activity.MainActivity.usernameContent;
import static com.example.duoduopin.tool.Constants.loginUrl;

public class LoginActivity extends AppCompatActivity {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private String username;

    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .pingInterval(10, TimeUnit.SECONDS)
            .build();

    private EditText usernameInput;
    private EditText passwordInput;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.phoneNumberInput);
        passwordInput = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginButton);
        TextView toRegisterButton = findViewById(R.id.toRegisterButton);
        toRegisterButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), RegisterActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> {
            username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();

            JSONObject jwt = new JSONObject();
            try {
                jwt.put("username", username);
                jwt.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                int SDK_INT = Build.VERSION.SDK_INT;
                if (SDK_INT > 8) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    final int res = postRequest(jwt.toString());
                    if (res == 1) {
                        setResult(RESULT_OK, null);
                        finish();
                    } else if (res == 2) {
                        Toast.makeText(v.getContext(), "用户名或密码错误！", Toast.LENGTH_SHORT).show();
                    } else if (res == 3) {
                        Toast.makeText(v.getContext(), "请先注册！", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(v.getContext(), RegisterActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(v.getContext(), "未知错误", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressLint("CommitPrefEdits")
    @RequiresApi(api = Build.VERSION_CODES.R)
    private int postRequest(String jsonString) throws IOException, JSONException {

        final String TAG = "LoginActivity";
        final String idFromServer = "userId";
        final String tokenFromServer = "token";
        final String nicknameFromServer = "nickname";

        int ret = 0;

        RequestBody body = RequestBody.create(jsonString, JSON);
        Request request = new Request.Builder()
                .url(loginUrl)
                .post(body)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        JSONObject responseJSON = new JSONObject(Objects.requireNonNull(response.body()).string());
        String codeString = responseJSON.getString("code");
        int code = Integer.parseInt(codeString);

        if (code == 100) {
            JSONObject contentJson = new JSONObject(responseJSON.getString("content"));
            Log.e("Login return value test", responseJSON.getString("content"));
            idContent = contentJson.optString(idFromServer);
            tokenContent = contentJson.optString(tokenFromServer);
            nicknameContent = contentJson.optString(nicknameFromServer);
            usernameContent = username;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("id", idContent);
            editor.putString("token", tokenContent);
            editor.putString("username", usernameContent);
            editor.putString("nickname", nicknameContent);
            editor.putLong("lastOnlineTime", System.currentTimeMillis() / 1000L);
            editor.apply();
            ret = 1;
        } else {
            Log.d(TAG, "responseJSON = " + responseJSON.toString());
            if (code == -1001) {
                ret = 2;
            } else if (code == -1000) {
                ret = 3;
            }
        }
        return ret;
    }
}