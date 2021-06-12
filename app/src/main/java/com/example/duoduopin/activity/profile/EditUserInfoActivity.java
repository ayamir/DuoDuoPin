package com.example.duoduopin.activity.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.duoduopin.R;

import static com.example.duoduopin.activity.MainActivity.nicknameContent;

public class EditUserInfoActivity extends AppCompatActivity {
    private String credit;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_info);

        loadInfo();
        bindItemAndOps();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void bindItemAndOps() {
        TextView tvEditNickname = findViewById(R.id.tv_edit_nickname);
        tvEditNickname.setText(nicknameContent);

        TextView tvEditCredit = findViewById(R.id.tv_edit_credit);
        tvEditCredit.setText(credit);

        ImageView ivEditBack = findViewById(R.id.iv_edit_back);
        ivEditBack.setOnClickListener(v -> finish());

        RelativeLayout rlEditNickname = findViewById(R.id.rl_edit_nickname);
        rlEditNickname.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditNicknameActivity.class);
            startActivity(intent);
        });
        RelativeLayout rlEditCredit = findViewById(R.id.rl_edit_credit);
        rlEditCredit.setOnClickListener(v -> Toast.makeText(v.getContext(), "信誉不能被修改哦！", Toast.LENGTH_SHORT).show());
        RelativeLayout rlEditHead = findViewById(R.id.rl_edit_head);
        rlEditHead.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditHeadActivity.class);
            startActivity(intent);
        });
        RelativeLayout rlEditPassword = findViewById(R.id.rl_edit_password);
        rlEditPassword.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void loadInfo() {
        Intent fromIntent = getIntent();
        if (fromIntent != null) {
            credit = fromIntent.getStringExtra("credit");
        }
    }
}
