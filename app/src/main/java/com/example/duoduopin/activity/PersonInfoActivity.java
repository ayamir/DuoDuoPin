package com.example.duoduopin.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.duoduopin.R;

public class PersonInfoActivity extends AppCompatActivity {
    private String nickname;
    private String credit;
    private String userId;
    private String headpath;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info);

        ImageView ivPersonBack = findViewById(R.id.iv_person_back);
        ivPersonBack.setOnClickListener(v -> finish());

        ImageView ivPersonHead = findViewById(R.id.iv_person_head);
        TextView tvPersonNickname1 = findViewById(R.id.tv_person_nickname1);
        TextView tvPersonNickname2 = findViewById(R.id.tv_person_nickname2);
        TextView tvPersonUserId1 = findViewById(R.id.tv_person_user_id1);
        TextView tvPersonUserId2 = findViewById(R.id.tv_person_user_id2);
        TextView tvPersonCredit = findViewById(R.id.tv_person_credit);

        boolean isInit = getDataFromIntent();
        if (isInit) {
            Bitmap head = BitmapFactory.decodeFile(headpath);
            ivPersonHead.setImageBitmap(head);

            tvPersonNickname1.setText(nickname);
            tvPersonNickname2.setText(nickname);

            tvPersonUserId1.setText(userId);
            tvPersonUserId2.setText(userId);

            tvPersonCredit.setText(credit);
        }
    }

    private boolean getDataFromIntent() {
        boolean res = false;
        Intent fromIntent = getIntent();
        if (fromIntent != null) {
            nickname = fromIntent.getStringExtra("nickname");
            credit = fromIntent.getStringExtra("credit");
            userId = fromIntent.getStringExtra("userId");
            headpath = fromIntent.getStringExtra("headpath");
            res = true;
        }
        return res;
    }
}
