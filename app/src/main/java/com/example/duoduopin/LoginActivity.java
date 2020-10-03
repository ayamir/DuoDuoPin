package com.example.duoduopin;

import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.SignupActivity.JSON;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private String urlLogin = "http://123.57.12.189:8080/User/login";

    private final OkHttpClient client = new OkHttpClient();

    private EditText accountEdit;

    private EditText passwordEdit;

    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        accountEdit = findViewById(R.id.account);
        passwordEdit = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(this);
    }

    private String createLoginJwt(String account, String password, long ttlMills) {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        long nowMills = System.currentTimeMillis();
        Date now = new Date(nowMills);

        Map<String, Object> claims = new HashMap<>();
        claims.put("account", account);
        claims.put("password", password);
        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .signWith(key);

        if (ttlMills > 0) {
            long expMills = nowMills + ttlMills;
            Date exp = new Date(expMills);
            builder.setExpiration(exp);
        }

        return builder.compact();
    }

    public static String crypt(String string) {
        if (string == null || string.length() == 0) {
            throw new IllegalArgumentException("String to encrypt can't be null or zero length");
        }
        StringBuffer hexString = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(string.getBytes());
            byte[] hash = md.digest();
            for (int i = 0; i < hash.length; i++) {
                if ((0xFF & hash[i]) < 0x10) {
                    hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
                } else {
                    hexString.append(Integer.toHexString(0xFF & hash[i]));
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hexString.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void postRequest(String url, String jsonBody) {
        RequestBody body = RequestBody.create(jsonBody, JSON);
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String content = Objects.requireNonNull(response.body()).string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("LoginResponse", content);
                        }
                    });
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View view) {
        String account = accountEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        password = crypt(password);

        long ttlMills = 2000000L;

        String jwt = createLoginJwt(account, password, ttlMills);

        postRequest(urlLogin, jwt);
    }
}