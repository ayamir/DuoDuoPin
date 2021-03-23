package com.example.duoduopin.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.activity.LoginActivity.JSON;
import static com.example.duoduopin.tool.Constants.registerUrl;

public class RegisterActivity extends AppCompatActivity {

    OkHttpClient client = new OkHttpClient().newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();

    private EditText usernameInput;
    private EditText nicknameInput;
    private EditText passwordInput;
    private EditText passwordReInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        usernameInput = findViewById(R.id.phoneNumberInput);
        nicknameInput = findViewById(R.id.nicknameInput);
        passwordInput = findViewById(R.id.passwordInput);
        passwordReInput = findViewById(R.id.passwordReInput);
        Button register = findViewById(R.id.registerButton);

        register.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString();
                String nickname = nicknameInput.getText().toString();
                String password = passwordInput.getText().toString();
                String passwordRepeat = passwordReInput.getText().toString();

                if (username.isEmpty()) {
                    Toast.makeText(v.getContext(), "请填写格式正确的手机号", Toast.LENGTH_SHORT).show();
                } else if (nickname.isEmpty()) {
                    Toast.makeText(v.getContext(), "请填写用户名", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty())  {
                    Toast.makeText(v.getContext(), "请输入密码", Toast.LENGTH_SHORT).show();
                } else if (passwordRepeat.isEmpty()) {
                    Toast.makeText(v.getContext(), "请输入确认密码", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(passwordRepeat)) {
                    Toast.makeText(v.getContext(), "两次密码输入不一致", Toast.LENGTH_SHORT).show();
                } else {
                    JSONObject jwt = new JSONObject();
                    //password = crypt(password);
                    Log.d("build Password", "onClick: " + password);
                    try {
                        jwt.put("username", username);
                        jwt.put("nickname", nickname);
                        jwt.put("password", password);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        int SDK_INT = Build.VERSION.SDK_INT;
                        if (SDK_INT > 8) {
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);
                            int state = putRequest(jwt.toString());
                            if (state == 1) {
                                Intent intent = new Intent(v.getContext(), LoginActivity.class);
                                startActivity(intent);
                                Toast.makeText(v.getContext(), "注册成功！", Toast.LENGTH_SHORT).show();
                            } else if (state == 2) {
                                Toast.makeText(v.getContext(), "此用户已存在！", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(v.getContext(), "未知错误", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public int putRequest(String jsonBody) throws IOException, JSONException {
        int ret = 0;
        final String TAG = "RegisterActivity";
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(registerUrl)
                .put(body)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        JSONObject responseJSON = new JSONObject(Objects.requireNonNull(response.body()).string());
        String codeString = responseJSON.getString("code");
        int code = Integer.parseInt(codeString);

        if (code == 100) {
            ret = 1;
        } else if (code == -1004) {
            ret = 2;
        }
        Log.d(TAG, "ret = " + ret);
        return ret;
    }

    /*
    public static String crypt(String string) {
        if (string == null || string.length() == 0) {
            throw new IllegalArgumentException("String to encrypt can't be null or zero length");
        }
        StringBuilder hexString = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(string.getBytes());
            byte[] hash = md.digest();
            for (byte b : hash) {
                if ((0xFF & b) < 0x10) {
                    hexString.append("0").append(Integer.toHexString((0xFF & b)));
                } else {
                    hexString.append(Integer.toHexString(0xFF & b));
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hexString.toString();
    }
    */
}