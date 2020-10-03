package com.example.duoduopin;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.SignupActivity.JSON;

public class LogoutActivity extends AppCompatActivity implements View.OnClickListener {

    private String urlLogout = "http://123.57.12.189:8080/User/logout";

    private Button logoutButton;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        logoutButton = findViewById(R.id.logout);
        logoutButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
//        delRequest(urlLogout, );
    }

    private void delRequest(String url, String account) {
        RequestBody body = RequestBody.create(account, JSON);
        final Request request = new Request.Builder()
                .url(url)
                .delete(body)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String content = Objects.requireNonNull(response.body()).string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("LogoutResponse", content);
                        }
                    });
                }
            }
        });
    }
}