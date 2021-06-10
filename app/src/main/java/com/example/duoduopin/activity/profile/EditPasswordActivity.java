package com.example.duoduopin.activity.profile;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.duoduopin.R;

public class EditPasswordActivity extends AppCompatActivity {
    private EditText etOldPassword;
    private EditText etNewPassword;
    private EditText etRepeatPassword;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_password);

        ImageView ivEditPasswordBack = findViewById(R.id.iv_edit_password_back);
        ivEditPasswordBack.setOnClickListener(v -> finish());

        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etRepeatPassword = findViewById(R.id.et_repeat_password);

        Button btnCommitPassword = findViewById(R.id.btn_commit_password);
        btnCommitPassword.setOnClickListener(v -> {
            boolean canCommit = true;
            canCommit = checkEmpty(v.getContext());
            if (canCommit) {
                canCommit = checkEqual(v.getContext());
            }
            if (canCommit) {
                int res = postChangePassword();
                if (res == 0) {
                    Toast.makeText(v.getContext(), "密码修改成功！", Toast.LENGTH_SHORT).show();
                } else if (res == 1) {
                    Toast.makeText(v.getContext(), "请检查旧密码是否正确！", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(v.getContext(), "遇到未知问题，请检查网络之后稍后再试！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private int postChangePassword() {
        int res = 0;
        // TODO: post to change password

        return res;
    }

    private boolean checkEqual(Context context) {
        boolean res = true;

        if (!etNewPassword.getText().equals(etRepeatPassword.getText()))
            res = false;

        return res;
    }

    private boolean checkEmpty(Context context) {
        boolean res = true;
        if (etOldPassword.getText().length() == 0) {
            res = false;
            Toast.makeText(context, "请先输入旧密码～", Toast.LENGTH_SHORT).show();
        }
        if (etNewPassword.getText().length() == 0) {
            res = false;
            Toast.makeText(context, "请先输入新密码～", Toast.LENGTH_SHORT).show();
        }
        if (etRepeatPassword.getText().length() == 0) {
            res = false;
            Toast.makeText(context, "请重复输入新密码～", Toast.LENGTH_SHORT).show();
        }
        return res;
    }
}
