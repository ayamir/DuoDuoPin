package com.example.duoduopin.activity;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.duoduopin.R;
import com.example.duoduopin.fragment.HomeFragment;
import com.example.duoduopin.fragment.MessageFragment;
import com.example.duoduopin.fragment.OrderFragment;
import com.example.duoduopin.fragment.ProfileFragment;
import com.example.duoduopin.service.JWebSocketClientService;
import com.example.duoduopin.service.JWebSocketClientService.JWebSocketClientBinder;
import com.example.duoduopin.tool.JWebSocketClient;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC;
import static com.example.duoduopin.activity.LoginActivity.idContent;
import static com.example.duoduopin.tool.Constants.getSysMsgUri;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private final String TAG = "MainActivity";
    private Context mContext;
    private final URI uri = getSysMsgUri(idContent);

    ImageView home, message, order, profile;

    private JWebSocketClient jWebSocketClient;
    private JWebSocketClientService.JWebSocketClientBinder binder;
    private JWebSocketClientService jWebSocketClientService;

    public static final OkHttpClient client = new OkHttpClient().newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected: 服务与活动绑定成功");
            binder = (JWebSocketClientBinder) service;
            jWebSocketClientService = binder.getService();
            jWebSocketClient = jWebSocketClientService.client;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected: 服务与活动断开成功");
        }
    };

    public MainActivity() throws URISyntaxException {
    }

    private void bindService() {
        Intent bindIntent = new Intent(MainActivity.this, JWebSocketClientService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    private class ChatMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
        }
    }

    private void startJWebSClientService() {
        Intent intent = new Intent(mContext, JWebSocketClientService.class);
        startService(intent);
    }

    private void doRegisterReceiver() {
        ChatMessageReceiver chatMessageReceiver = new ChatMessageReceiver();
        IntentFilter filter = new IntentFilter("com.example.duoduopin.servicecallback.content");
        registerReceiver(chatMessageReceiver, filter);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;

        startJWebSClientService();
        bindService();
        doRegisterReceiver();
        checkNotification(mContext);
        jWebSocketClient = new JWebSocketClient(uri) {
            @Override
            public void onMessage(String message) {
                Intent intent = new Intent();
                intent.setAction("com.example.duoduopin.servicecallback.content");
                intent.putExtra("message", message);
                sendBroadcast(intent);
            }
        };

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

    private void checkLockAndShowNotification(String content) {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.inKeyguardRestrictedInputMode()) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isScreenOn()) {
                @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
                wl.acquire(10 * 60 * 1000L /*10 minutes*/);
                wl.release();
            }
        }
        sendNotification(content);
    }

    private void sendNotification(String content) {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle("昵称")
                .setContentText(content)
                .setVisibility(VISIBILITY_PUBLIC)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_ALL | Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                .build();

        notifyManager.notify(1, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void checkNotification(final Context context) {
        if (!isNotificationEnabled(context)) {
            new AlertDialog.Builder(context).setTitle("温馨提示")
                    .setMessage("你还未开启系统通知，将影响消息的接收，要去开启吗？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setNotification(context);
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).show();
        }
    }

    private void setNotification(Context context) {
        Intent localIntent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            localIntent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            localIntent.putExtra("app_package", context.getPackageName());
            localIntent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            localIntent.addCategory(Intent.CATEGORY_DEFAULT);
            localIntent.setData(Uri.parse("package:" + context.getPackageName()));
        } else {
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        }
        context.startActivity(localIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean isNotificationEnabled(Context context) {
        String CHECK_OP_NO_THROW = "checkOpNoThrow";
        String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;

        Class appOpsClass = null;
        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(
                    CHECK_OP_NO_THROW,
                    Integer.TYPE,
                    Integer.TYPE,
                    String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
            int value = (Integer) opPostNotificationValue.get(Integer.class);
            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);

        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return false;
    }
}
