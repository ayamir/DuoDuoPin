package com.example.duoduopin.activity;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.example.duoduopin.R;
import com.example.duoduopin.fragment.HomeFragment;
import com.example.duoduopin.fragment.MessageFragment;
import com.example.duoduopin.fragment.OrderFragment;
import com.example.duoduopin.fragment.ProfileFragment;
import com.example.duoduopin.service.RecSysMsgService;
import com.example.duoduopin.tool.MyDBHelper;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    ImageView home, message, order, profile;

    public static SharedPreferences prefs;
    public static final String prefName = "tokenData";

    public static final OkHttpClient client = new OkHttpClient().newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();

    public static boolean isMessageClicked = false;

    public static String idContent;
    public static String tokenContent;
    public static String usernameContent;
    public static String nicknameContent;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            Log.e("MainActivity", "onServiceConnected");
            RecSysMsgService.RecSysMsgBinder binder = (RecSysMsgService.RecSysMsgBinder) iBinder;
            RecSysMsgService recSysMsgService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("MainActivity", "onServiceDisconnected");
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLogin();

        initListener();
        initView();
    }

    private void checkLogin() {
        prefs = getSharedPreferences(prefName, Context.MODE_PRIVATE);
        String id = prefs.getString("id", "");
        String token = prefs.getString("token", "");
        String username = prefs.getString("username", "");
        String nickname = prefs.getString("nickname", "");
        Long lastOnlineTime = prefs.getLong("lastOnlineTime", 0);
        Long nowTime = System.currentTimeMillis() / 1000L;
        long timeOffset = (nowTime - lastOnlineTime) / (3600 * 24);
        if (nickname.isEmpty() || id.isEmpty() || username.isEmpty() || token.isEmpty()) {
            Toast.makeText(this, "请先登录！", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 1);
        } else if (timeOffset > 2) {
            Toast.makeText(this, "登录已过期，请重新登录！", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 1);
        } else {
            idContent = id;
            tokenContent = token;
            usernameContent = username;
            nicknameContent = nickname;
            Intent bindIntent = new Intent(this, RecSysMsgService.class);
            bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "登录成功！", Toast.LENGTH_SHORT).show();
            Intent bindIntent = new Intent(this, RecSysMsgService.class);
            bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    private void initView() {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.mainBody, new HomeFragment());
        fragmentTransaction.commit();
        setButtonColor(0);
    }

    private void initListener() {
        setContentView(R.layout.activity_main);

        home = findViewById(R.id.homeView);
        message = findViewById(R.id.messageView);
        order = findViewById(R.id.orderView);
        profile = findViewById(R.id.profileView);

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
