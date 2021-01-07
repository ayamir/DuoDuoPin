package com.example.duoduopin.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
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

public class LoginActivity extends AppCompatActivity {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");


    private SharedPreferences pref;
    public static String idContent;
    public static String tokenContent;
    public static String nameContent;
    public static String nicknameContent;

    private String username;

    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();

    private EditText usernameInput;
    private EditText passwordInput;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        String prefName = "tokenData";
        pref = getSharedPreferences(prefName, Context.MODE_PRIVATE);

        Intent intent = getIntent();
        if (intent != null) {
            String isLogoutIntent;
            isLogoutIntent = intent.getStringExtra("logout");
            if (isLogoutIntent != null) {
                if (isLogoutIntent.equals("true")) {
                    pref.edit().clear();
                    pref.edit().apply();
                }
            }
        }

        usernameInput = findViewById(R.id.phoneNumberInput);
        passwordInput = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginButton);
        TextView toRegisterButton = findViewById(R.id.toRegisterButton);
        toRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();

                //String jwt = createLoginJwt(account, password, ttlMills);
                JSONObject jwt = new JSONObject();
                try {
                    jwt.put("username", username);
                    jwt.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String urlLogin = "http://123.57.12.189:8080/User/login";
                try {
                    int SDK_INT = Build.VERSION.SDK_INT;
                    if (SDK_INT > 8) {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                        int res = postRequest(urlLogin, jwt.toString());
                        if (res == 1) {
                            Intent intent = new Intent(v.getContext(), MainActivity.class);
                            startActivity(intent);
                            Toast.makeText(v.getContext(), "登录成功！", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressLint("CommitPrefEdits")
    @RequiresApi(api = Build.VERSION_CODES.R)
    private int postRequest(String url, String jsonBody) throws IOException, JSONException {
        int ret = 0;
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        if (response.code() == 200) {
            JSONObject responseJson = new JSONObject(Objects.requireNonNull(response.body()).string());
            JSONObject contentJson = new JSONObject(responseJson.getString("content"));
            idContent = contentJson.optString("id");
            tokenContent = contentJson.optString("token");
            nicknameContent = contentJson.optString("nickname");
            nameContent = username;
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("id", idContent);
            editor.putString("token", tokenContent);
            editor.putString("name", nameContent);
            ret = 1;
        } else {
            response = response.newBuilder().removeHeader("Cache-Control").build();
            Log.d("Login", response.toString());
        }
        return ret;
    }
}