package com.example.duoduopin;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener{

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    private String urlSignUp = "http://123.57.12.189:8080/User/register";

    private EditText accountSign;
    private EditText nicknameSign;

    private EditText passwordSign;
    private EditText passwordSignRepeat;

    private Button signButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        accountSign = findViewById(R.id.accountSign);
        nicknameSign = findViewById(R.id.nickName);
        passwordSign = findViewById(R.id.passwordSign);
        passwordSignRepeat = findViewById(R.id.passwordSignRepeat);
        signButton = findViewById(R.id.signUp);

        signButton.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View view) {
        String account = accountSign.getText().toString();
        String nickname = nicknameSign.getText().toString();
        String password = passwordSign.getText().toString();
        String passwordRepeat = passwordSignRepeat.getText().toString();

        if (!password.equals(passwordRepeat)) {
            Toast.makeText(this, "两次密码输入不一致", Toast.LENGTH_SHORT).show();
        } else {
            String jwt = createSignJwt(account, nickname, password);

            try {
                putRequest(urlSignUp, jwt);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String createSignJwt(String account, String nickname, String password) {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        long nowMills = System.currentTimeMillis();
        Date now = new Date(nowMills);

        Map<String, Object> claimSign = new HashMap<>();
        claimSign.put("account", account);
        claimSign.put("nickname", nickname);
        claimSign.put("password", password);
        JwtBuilder builder = Jwts.builder()
                .setClaims(claimSign)
                .setIssuedAt(now)
                .signWith(key);

        return builder.compact();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void putRequest(String url, String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String content = Objects.requireNonNull(response.body().string());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("SignResponse", content);
                        }
                    });
                }
            }
        });
    }
}