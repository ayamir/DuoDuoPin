package com.example.duoduopin.activity.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.duoduopin.R;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.activity.MainActivity.client;
import static com.example.duoduopin.activity.MainActivity.idContent;
import static com.example.duoduopin.activity.MainActivity.tokenContent;
import static com.example.duoduopin.handler.GeneralMsgHandler.ERROR;
import static com.example.duoduopin.handler.GeneralMsgHandler.SUCCESS;
import static com.example.duoduopin.tool.Constants.changePasswdUrl;

public class EditPasswordActivity extends AppCompatActivity {
    private EditText etOldPassword;
    private EditText etNewPassword;
    private EditText etRepeatPassword;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        ImageView ivEditPasswordBack = findViewById(R.id.iv_edit_password_back);
        ivEditPasswordBack.setOnClickListener(v -> finish());

        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etRepeatPassword = findViewById(R.id.et_repeat_password);

        Button btnCommitPassword = findViewById(R.id.btn_commit_password);
        btnCommitPassword.setOnClickListener(v -> {
            boolean canCommit;
            canCommit = checkEmpty(v.getContext());
            if (canCommit) {
                canCommit = checkEqual();
            } else {
                Toast.makeText(v.getContext(), "请检查密码是否一致~", Toast.LENGTH_SHORT).show();
            }
            if (canCommit) {
                new Thread(new Runnable() {
                    @SuppressLint("HandlerLeak")
                    final Handler changePasswordHandler = new Handler() {
                        @Override
                        public void handleMessage(@NonNull Message msg) {
                            if (msg.what == SUCCESS) {
                                Toast.makeText(v.getContext(), "密码修改成功！", Toast.LENGTH_SHORT).show();
                            } else if (msg.what == ERROR) {
                                Toast.makeText(v.getContext(), "请检查旧密码是否正确！", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(v.getContext(), "遇到未知问题，请检查网络之后稍后再试！", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };

                    @Override
                    public void run() {
                        try {
                            Message message = new Message();
                            message.what = postChangePassword();
                            changePasswordHandler.sendMessage(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    private int postChangePassword() throws IOException {
        int res;

        final String old = etOldPassword.getText().toString();
        final String now = etNewPassword.getText().toString();

        RequestBody body = new FormBody.Builder()
                .add("old", old)
                .add("new", now)
                .build();

        final Request request = new Request.Builder()
                .url(changePasswdUrl)
                .header("token", idContent + "_" + tokenContent)
                .post(body)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            res = SUCCESS;
        } else {
            res = ERROR;
        }

        return res;
    }

    private boolean checkEqual() {
        boolean res = true;

        if (!etNewPassword.getText().equals(etRepeatPassword.getText()))
            res = false;

        return res;
    }

    private boolean checkEmpty(Context context) {
        boolean res = true;
        if (etOldPassword.getText().length() == 0) {
            res = false;
            Toast.makeText(context, "请先输入旧密码~", Toast.LENGTH_SHORT).show();
        }
        if (etNewPassword.getText().length() == 0) {
            res = false;
            Toast.makeText(context, "请先输入新密码~", Toast.LENGTH_SHORT).show();
        }
        if (etRepeatPassword.getText().length() == 0) {
            res = false;
            Toast.makeText(context, "请重复输入新密码~", Toast.LENGTH_SHORT).show();
        }
        return res;
    }
}
