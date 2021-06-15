package com.example.duoduopin.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.duoduopin.R;
import com.example.duoduopin.fragment.main.HomeFragment;
import com.example.duoduopin.fragment.main.MessageFragment;
import com.example.duoduopin.fragment.main.OrderFragment;
import com.example.duoduopin.fragment.main.ProfileFragment;
import com.example.duoduopin.pojo.BriefOrderContent;
import com.example.duoduopin.pojo.OrderContent;
import com.example.duoduopin.service.RecSysMsgService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liulishuo.filedownloader.FileDownloader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.example.duoduopin.activity.LoginActivity.JSON;
import static com.example.duoduopin.handler.GeneralMsgHandler.SUCCESS;
import static com.example.duoduopin.tool.Constants.API_KEY_TO_MAP;
import static com.example.duoduopin.tool.Constants.brief_order_content_load_signal;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    public static final String prefName = "tokenData";
    public static final String basePath = Environment.getExternalStoragePublicDirectory("Download").getAbsolutePath();
    public static final OkHttpClient client = new OkHttpClient().newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();
    public static SharedPreferences prefs;
    public static boolean isMessageClicked = false;
    public static boolean isLoaded = false;
    public static String idContent;
    public static String tokenContent;
    public static String usernameContent;
    public static String nicknameContent;
    public static String creditContent;
    public static String lastLatitudeContent;
    public static String lastLongitudeContent;
    public static String headUrl;
    public static String headPath = "";
    public static Bitmap head;
    public static ArrayList<OrderContent> recOrderContentList;
    private final int LOCATION_REQUEST_CODE = 1;


    @SuppressLint("HandlerLeak")
    private final Handler locateSuccessHandler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == SUCCESS) {
                new Thread(() -> {
                    try {
                        postQueryRecOrderList();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    };

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
    private ImageView home, message, order, profile;
    private boolean isConnected = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileDownloader.setup(this);
        checkLogin();

        initListener();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isConnected) {
            unbindService(connection);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkLogin() {
        prefs = getSharedPreferences(prefName, Context.MODE_PRIVATE);
        String id = prefs.getString("id", "");
        String token = prefs.getString("token", "");
        String username = prefs.getString("username", "");
        String nickname = prefs.getString("nickname", "");
        String credit = prefs.getString("credit", "");
        String lastLatitude = prefs.getString("lastLatitude", "");
        String lastLongitude = prefs.getString("lastLongitude", "");
        headPath = prefs.getString("headPath", "");
        Long lastOnlineTime = prefs.getLong("lastOnlineTime", 0);
        Long nowTime = System.currentTimeMillis() / 1000L;
        long timeOffset = (nowTime - lastOnlineTime) / (3600 * 24);
        if (nickname.isEmpty() || id.isEmpty() || username.isEmpty() || token.isEmpty()) {
            Toast.makeText(this, "请先登录！", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 1);
        } else if (timeOffset > 3) {
            Toast.makeText(this, "登录已过期，请重新登录！", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 1);
        } else {
            idContent = id;
            tokenContent = token;
            usernameContent = username;
            nicknameContent = nickname;
            creditContent = credit;
            lastLatitudeContent = lastLatitude;
            lastLongitudeContent = lastLongitude;
            if (!headPath.isEmpty()) {
                head = BitmapFactory.decodeFile(headPath);
            }
            if (lastLatitudeContent.isEmpty() || lastLongitudeContent.isEmpty()) {
                if (checkPermission()) {
                    getLocatePermission();
                } else {
                    startLocationRequest();
                }
            } else {
                new Thread(() -> {
                    Message message = new Message();
                    message.what = SUCCESS;
                    locateSuccessHandler.sendMessage(message);
                }).start();
            }

            Intent bindIntent = new Intent(this, RecSysMsgService.class);
            bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
            isConnected = true;
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isCoarseLocationAllowed = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED;
            boolean isFineLocationAllowed = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED;
            boolean isNetworkLocationAllowed = checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PERMISSION_GRANTED;
            boolean isWifiLocationAllowed = checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PERMISSION_GRANTED;
            boolean isChangeWifiLocationAllowed = checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) == PERMISSION_GRANTED;
            boolean isInternetAllowed = checkSelfPermission(Manifest.permission.INTERNET) == PERMISSION_GRANTED;
            boolean isReadPhoneStateAllowed = checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PERMISSION_GRANTED;
            boolean isWriteSDAllowed = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
            return !isReadPhoneStateAllowed || !isFineLocationAllowed || !isCoarseLocationAllowed || !isNetworkLocationAllowed ||
                    !isWifiLocationAllowed || !isChangeWifiLocationAllowed || !isInternetAllowed || !isWriteSDAllowed;
        }
        return true;
    }

    void getLocatePermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        }, LOCATION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            boolean isGranted = true;
            int i = 0;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    isGranted = false;
                    Toast.makeText(this, "Permission Denied: " + permissions[i], Toast.LENGTH_SHORT).show();
                    break;
                }
                i++;
            }
            if (isGranted) {
                startLocationRequest();
            } else {
                Toast.makeText(this, "请同意上述权限来获得更好的应用体验！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationRequest() {
        AMapLocationClient.setApiKey(API_KEY_TO_MAP);
        AMapLocationClient locationClient = new AMapLocationClient(this);

        AMapLocationClientOption locationClientOption = new AMapLocationClientOption();
        locationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationClientOption.setOnceLocation(true);
        locationClientOption.setOnceLocationLatest(true);
        locationClientOption.setLocationCacheEnable(false);
        locationClient.setLocationOption(locationClientOption);

        AMapLocationListener locationListener = aMapLocation -> {
            final String TAG = "AMapError";
            if (aMapLocation != null) {
                Log.e(TAG, "aMapLocation has response");
                if (aMapLocation.getErrorCode() == 0) {
                    lastLatitudeContent = String.valueOf(aMapLocation.getLatitude());
                    lastLongitudeContent = String.valueOf(aMapLocation.getLongitude());
                    Log.e(TAG, "latitude = " + lastLatitudeContent + ", longitude = " + lastLongitudeContent);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("lastLatitude", lastLatitudeContent);
                    editor.putString("lastLongitude", lastLongitudeContent);
                    editor.apply();
                    new Thread(() -> {
                        Message message = new Message();
                        message.what = SUCCESS;
                        locateSuccessHandler.sendMessage(message);
                    }).start();
                } else {
                    Log.e(TAG, "location error, error code: " + aMapLocation.getErrorCode() + "\nerror info: " + aMapLocation.getErrorInfo());
                }
            } else {
                Log.e(TAG, "location error");
            }
        };
        locationClient.setLocationListener(locationListener);
        locationClient.startLocation();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void postQueryRecOrderList() throws IOException, JSONException {
        final String TAG = "getRecOrderList";

        if (lastLongitudeContent != null) {
            JSONObject bodyJSON = new JSONObject();
            bodyJSON.put("latitude", lastLatitudeContent);
            bodyJSON.put("longitude", lastLongitudeContent);

            RequestBody body = RequestBody.create(bodyJSON.toString(), JSON);

            Request request = new Request.Builder()
                    .url(com.example.duoduopin.tool.Constants.queryByInfoUrl)
                    .header("token", idContent + "_" + tokenContent)
                    .post(body)
                    .build();

            Call call = client.newCall(request);
            Response response = call.execute();

            if (response.code() == 200) {
                JSONObject responseJSON = new JSONObject(Objects.requireNonNull(response.body()).string());
                recOrderContentList = new Gson().fromJson(responseJSON.getString("content"), new TypeToken<List<OrderContent>>() {
                }.getType());
                if (recOrderContentList != null) {
                    for (OrderContent orderContent : recOrderContentList) {
                        long oldTime = Long.parseLong(orderContent.getTime());
                        String newTime = Instant.ofEpochMilli(oldTime).atZone(ZoneOffset.ofHours(8)).toLocalDateTime().toString().replace('T', ' ');
                        orderContent.setTime(newTime);
                    }
                }
                Intent intent = new Intent();
                intent.putExtra("BriefOrderContentLoaded", SUCCESS);
                intent.setAction(brief_order_content_load_signal);
                sendBroadcast(intent);
            } else {
                Log.d(TAG, Objects.requireNonNull(response.body()).string());
                Log.d(TAG, response.toString());
            }
        } else {
            Log.e(TAG, "latitude and longitude is null");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "登录成功！", Toast.LENGTH_SHORT).show();

            if (checkPermission()) {
                getLocatePermission();
            } else {
                startLocationRequest();
            }

            Intent bindIntent = new Intent(this, RecSysMsgService.class);
            bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
            isConnected = true;
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
