package com.example.duoduopin.activity.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.duoduopin.R;

public class EditNicknameActivity extends AppCompatActivity {
    private String nickname;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_nickname);

        loadInfo();
        bindItemAndOps();
    }

    private void bindItemAndOps() {
        ImageView ivEditNicknameBack = findViewById(R.id.iv_edit_nickname_back);
        ivEditNicknameBack.setOnClickListener(v -> finish());

        EditText etEditNickname = findViewById(R.id.et_edit_nickname);
        etEditNickname.setHint(nickname);

        Button btnCommitNickname = findViewById(R.id.btn_commit_nickname);
        btnCommitNickname.setOnClickListener(v -> {
            if (etEditNickname.getText().length() == 0) {
                Toast.makeText(v.getContext(), "请先输入新昵称～", Toast.LENGTH_SHORT).show();
            } else {

            }
        });
    }

    private void loadInfo() {
        Intent fromIntent = getIntent();
        if (fromIntent != null) {
            nickname = fromIntent.getStringExtra("nickname");
        }
    }
}
