package com.example.duoduopin.activity;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.example.duoduopin.R;
import com.example.duoduopin.fragment.HomeFragment;
import com.example.duoduopin.fragment.MessageFragment;
import com.example.duoduopin.fragment.OrderFragment;
import com.example.duoduopin.fragment.ProfileFragment;
import com.example.duoduopin.tool.MyDBHelper;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    ImageView home, message, order, profile;

    public static final OkHttpClient client = new OkHttpClient().newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();

    public static boolean isMessageClicked = false;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        home = findViewById(R.id.homeView);
        message = findViewById(R.id.messageView);
        order = findViewById(R.id.orderView);
        profile = findViewById(R.id.profileView);

        initListener();
        initView();
    }

    private void initView() {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.mainBody, new HomeFragment());
        fragmentTransaction.commit();
        setButtonColor(0);
    }

    private void initListener() {
        home.setOnClickListener(this);
        message.setOnClickListener(this);
        order.setOnClickListener(this);
        profile.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();

        Fragment fragment = null;
        switch (v.getId()) {
            case R.id.homeView:
                fragment = new HomeFragment();
                setButtonColor(0);
                break;
            case R.id.messageView:
                fragment = new MessageFragment();
                setButtonColor(1);
                isMessageClicked = true;
                break;
            case R.id.orderView:
                fragment = new OrderFragment();
                setButtonColor(2);
                break;
            case R.id.profileView:
                fragment = new ProfileFragment();
                setButtonColor(3);
                break;
        }

        fragmentTransaction.replace(R.id.mainBody, fragment);
        fragmentTransaction.commit();
    }

    private void setButtonColor(int buttonId) {
        switch (buttonId) {
            case 0:
                home.setImageResource(R.drawable.home_selected);
                message.setImageResource(R.drawable.message);
                order.setImageResource(R.drawable.order);
                profile.setImageResource(R.drawable.profile);
                break;
            case 1:
                home.setImageResource(R.drawable.home);
                message.setImageResource(R.drawable.message_selected);
                order.setImageResource(R.drawable.order);
                profile.setImageResource(R.drawable.profile);
                break;
            case 2:
                home.setImageResource(R.drawable.home);
                message.setImageResource(R.drawable.message);
                order.setImageResource(R.drawable.order_selected);
                profile.setImageResource(R.drawable.profile);
                break;
            case 3:
                home.setImageResource(R.drawable.home);
                message.setImageResource(R.drawable.message);
                order.setImageResource(R.drawable.order);
                profile.setImageResource(R.drawable.profile_selected);
                break;
        }
    }
}
