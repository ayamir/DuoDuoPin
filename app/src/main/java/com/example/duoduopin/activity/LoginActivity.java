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
import com.example.duoduopin.tool.MyDBHelper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import static com.example.duoduopin.tool.Constants.getSysMsgUrl;
import static com.example.duoduopin.tool.Constants.loginUrl;

public class LoginActivity extends AppCompatActivity {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private SharedPreferences pref;
    public static String idContent;
    public static String tokenContent;
    public static String nameContent;
    public static String nicknameContent;

    private String username;

    private final MyDBHelper myDBHelper = new MyDBHelper(this, "DuoDuoPin.db", null, 1);

    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .pingInterval(10, TimeUnit.SECONDS)
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
                            Intent intent = new Intent(v.getContext(), MainActivity.class);
                            startActivity(intent);
                            myDBHelper.getWritableDatabase();
                            Toast.makeText(v.getContext(), "登录成功！", Toast.LENGTH_SHORT).show();
                            Log.e("Login Succeed", "idContent = " + idContent + ", TokenContent = " + tokenContent);
                            Request request = new Request.Builder()
                                    .url(getSysMsgUrl(idContent))
                                    .header("token", idContent + "_" + tokenContent)
                                    .build();
                            final String TAG = "SysMsg WebSocket";
                            WebSocket mWebSocket = client.newWebSocket(request, new WebSocketListener() {
                                final ExecutorService writeExecutor = Executors.newSingleThreadExecutor();
                                WebSocket webSocket = null;
                                @Override
                                public void onOpen(@NotNull final WebSocket webSocket, @NotNull Response response) {
                                    super.onOpen(webSocket, response);
                                    Log.e(TAG, "onOpen: SysMsg WebSocket opened!");
                                    this.webSocket = webSocket;
                                    writeExecutor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            webSocket.send("这里是客户端");
                                        }
                                    });
                                }

                                @Override
                                public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                                    super.onClosed(webSocket, code, reason);
                                    Log.e(TAG, "onOpen: SysMsg WebSocket closed, because of " + code + "/" + reason);
                                    writeExecutor.shutdown();
                                }

                                @Override
                                public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                                    super.onClosing(webSocket, code, reason);
                                    Log.e(TAG, "onOpen: SysMsg WebSocket is closing, because of " + code + "/" + reason);
                                }

                                @Override
                                public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                                    super.onFailure(webSocket, t, response);
                                }

                                @Override
                                public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                                    super.onMessage(webSocket, text);
                                    Log.e(TAG, "onMessage: new message is " + text);
                                }
                            });
                        } else if (res == 2) {
                            Toast.makeText(v.getContext(), "用户名或密码错误！", Toast.LENGTH_SHORT).show();
                        } else if (res == 3) {
                            Toast.makeText(v.getContext(), "请先注册！", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(v.getContext(),RegisterActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(v.getContext(), "未知错误", Toast.LENGTH_SHORT).show();
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
            idContent = contentJson.optString(idFromServer);
            tokenContent = contentJson.optString(tokenFromServer);
            nicknameContent = contentJson.optString(nicknameFromServer);
            nameContent = username;
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("id", idContent);
            editor.putString("token", tokenContent);
            editor.putString("name", nameContent);
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