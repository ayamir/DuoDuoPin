package com.example.duoduopin.activity.order;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.duoduopin.R;
import com.example.duoduopin.adapter.OrderTabAdapter;
import com.example.duoduopin.pojo.BriefMemberInfo;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.activity.MainActivity.basePath;
import static com.example.duoduopin.activity.MainActivity.client;
import static com.example.duoduopin.activity.MainActivity.idContent;
import static com.example.duoduopin.activity.MainActivity.tokenContent;
import static com.example.duoduopin.handler.GeneralMsgHandler.ERROR;
import static com.example.duoduopin.handler.GeneralMsgHandler.FAILED;
import static com.example.duoduopin.handler.GeneralMsgHandler.GROUP_FULL;
import static com.example.duoduopin.handler.GeneralMsgHandler.JOIN_REPEAT;
import static com.example.duoduopin.handler.GeneralMsgHandler.SUCCESS;
import static com.example.duoduopin.tool.Constants.getDelOrderUrl;
import static com.example.duoduopin.tool.Constants.getImageDownloadUrl;
import static com.example.duoduopin.tool.Constants.getJoinUrl;
import static com.example.duoduopin.tool.Constants.getQueryMemberUrl;
import static com.example.duoduopin.tool.Constants.getQuitUrl;
import static com.example.duoduopin.tool.Constants.group_quit_signal;

public class OneOrderCaseActivity extends FragmentActivity {

    private final String MAIN_TAB_CONTENT = "拼单内容";
    private final String DETAILS_TAB_CONTENT = "拼单详情";
    private final String MEMBER_TAB_CONTENT = "拼单成员";

    private final ArrayList<BriefMemberInfo> memberInfoList = new ArrayList<>();
    private final ArrayList<String> memberIds = new ArrayList<>();

    private final OrderTabAdapter tabAdapter = new OrderTabAdapter(this);
    private String userIdString;
    private String nicknameString;
    private String orderIdString;
    private String typeString;
    private String priceString;
    private String addressString;
    private String curPeopleString;
    private String maxPeopleString;
    private String timeString;
    private String descriptionString;
    private String titleString;
    private String imageUrl = "";
    private String imagePath = "";
    private Button btnDelete, btnJoin, btnLeave;
    private ImageView ivBack;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;

    public static String getDownloadPath(String memberHeadUrl, String memberId) {
        if (!memberHeadUrl.equals("null")) {
            String format = memberHeadUrl.substring(memberHeadUrl.lastIndexOf('.'));
            String filepath = basePath + File.separator + memberId + "_head" + format;
            FileDownloader.getImpl().create(memberHeadUrl).setPath(filepath).start();
            return filepath;
        } else {
            return "";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_order_case);
        getInfoFromIntent();
        bindItems();
        bindOperation();
        setVisibility();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setVisibility() {
        if (userIdString.equals(idContent)) {
            btnDelete.setVisibility(View.VISIBLE);
            btnLeave.setVisibility(View.INVISIBLE);
            btnJoin.setVisibility(View.INVISIBLE);
        } else {
            btnDelete.setVisibility(View.INVISIBLE);
        }

        @SuppressLint("HandlerLeak") final Handler isInHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == SUCCESS) {
                    if (isInMembers()) {
                        if (userIdString.equals(idContent))
                            btnLeave.setVisibility(View.INVISIBLE);
                        btnJoin.setVisibility(View.INVISIBLE);
                    } else {
                        btnLeave.setVisibility(View.INVISIBLE);
                        btnJoin.setVisibility(View.VISIBLE);
                    }

                    int MEMBER_POS = 2;
                    tabAdapter.setBundle(setMemberBundle(), MEMBER_POS);
                }
            }
        };

        new Thread(() -> {
            Message message = new Message();
            try {
                message.what = postQueryGrpMem(getQueryMemberUrl(orderIdString));
                isInHandler.sendMessage(message);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();


        @SuppressLint("HandlerLeak") final Handler downloadImageHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == SUCCESS) {
                    int MAIN_POS = 0;
                    tabAdapter.setBundle(setMainBundle(), MAIN_POS);

                    int DETAILS_POS = 1;
                    tabAdapter.setBundle(setDetailsBundle(), DETAILS_POS);

                    viewPager2.setAdapter(tabAdapter);

                    new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
                        switch (position) {
                            case 1:
                                tab.setText(DETAILS_TAB_CONTENT);
                                break;
                            case 2:
                                tab.setText(MEMBER_TAB_CONTENT);
                                break;
                            default:
                                tab.setText(MAIN_TAB_CONTENT);
                                break;
                        }
                    }).attach();
                }
            }
        };

        String TAG = "downloadImage";
        new Thread(() -> {
            Message msg = new Message();
            try {
                int signal = postGetImageUrl();
                msg.what = signal;
                if (signal == SUCCESS) {
                    Log.e(TAG, imageUrl);
                    imagePath = downloadImage(imageUrl);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            downloadImageHandler.sendMessage(msg);
        }).start();
    }

    private String downloadImage(String imageUrl) {
        String TAG = "downloadImage";
        String filepath = "";
        if (!imageUrl.isEmpty() && !imageUrl.equals("null")) {
            String format = imageUrl.substring(imageUrl.lastIndexOf('.'));
            filepath = basePath + File.separator + orderIdString + "_order" + format;
            FileDownloader.getImpl().create(imageUrl).setPath(filepath).setListener(new FileDownloadListener() {
                @Override
                protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                }

                @Override
                protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    Log.e(TAG, "task id: " + task.getId() + " soFarBytes: " + soFarBytes + ", totalBytes: " + totalBytes);
                }

                @Override
                protected void completed(BaseDownloadTask task) {
                    Log.e(TAG, "task id: " + task.getId() + " completed, path: " + task.getPath());
                }

                @Override
                protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                }

                @Override
                protected void error(BaseDownloadTask task, Throwable e) {
                    Log.e(TAG, "task id: " + task.getId() + " error!");
                }

                @Override
                protected void warn(BaseDownloadTask task) {

                }
            }).start();
        }
        return filepath;
    }

    private boolean isInMembers() {
        return memberIds.contains(idContent);
    }

    private void getInfoFromIntent() {
        Intent fromIntent = getIntent();
        if (fromIntent != null) {
            orderIdString = fromIntent.getStringExtra("orderId");
            Log.e("infoFromIntent", orderIdString);
            if (orderIdString != null) {
                userIdString = fromIntent.getStringExtra("userId");
                nicknameString = fromIntent.getStringExtra("nickname");
                typeString = fromIntent.getStringExtra("type").equals("BILL") ? "拼单" : "拼车";
                priceString = fromIntent.getStringExtra("price");
                addressString = fromIntent.getStringExtra("address");
                curPeopleString = fromIntent.getStringExtra("curPeople");
                maxPeopleString = fromIntent.getStringExtra("maxPeople");
                timeString = fromIntent.getStringExtra("time").replace('T', ' ');
                descriptionString = fromIntent.getStringExtra("description");
                titleString = fromIntent.getStringExtra("title");
                if (!typeString.equals("拼车")) {
                    imageUrl = fromIntent.getStringExtra("imageUrl");
                }
            }
        }
    }

    private Bundle setMainBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("type", typeString);
        bundle.putString("imagePath", imagePath);
        bundle.putString("price", priceString);
        bundle.putString("title", titleString);
        bundle.putString("description", descriptionString);
        bundle.putString("nickname", nicknameString);

        return bundle;
    }

    private Bundle setDetailsBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("userId", userIdString);
        bundle.putString("type", typeString);
        bundle.putString("orderId", orderIdString);
        bundle.putString("price", priceString);
        bundle.putString("curPeople", curPeopleString);
        bundle.putString("maxPeople", maxPeopleString);
        bundle.putString("time", timeString);
        bundle.putString("address", addressString);

        return bundle;
    }

    private Bundle setMemberBundle() {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("memberInfoList", memberInfoList);
        return bundle;
    }

    @Override
    public void onBackPressed() {
        if (viewPager2.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            viewPager2.setCurrentItem(viewPager2.getCurrentItem() - 1);
        }
    }

    private void bindItems() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager2 = findViewById(R.id.viewpager2);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        btnJoin = findViewById(R.id.btn_join);
        btnLeave = findViewById(R.id.btn_leave);
        btnDelete = findViewById(R.id.btn_delete);
        ivBack = findViewById(R.id.btn_back);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void bindOperation() {
        btnJoin.setOnClickListener(v -> new Thread(new Runnable() {
            @SuppressLint("HandlerLeak")
            final Handler joinHandler = new Handler() {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    switch (msg.what) {
                        case SUCCESS:
                            Toast.makeText(v.getContext(), "请求发送成功！", Toast.LENGTH_SHORT).show();
                            break;
                        case ERROR:
                            Toast.makeText(v.getContext(), "请检查网络状况稍后再试", Toast.LENGTH_SHORT).show();
                            break;
                        case JOIN_REPEAT:
                            Toast.makeText(v.getContext(), "您已发送过请求，无需重复发送！", Toast.LENGTH_SHORT).show();
                            break;
                        case GROUP_FULL:
                            Toast.makeText(v.getContext(), "当前小组人数已满！", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                }
            };

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    Message message = new Message();
                    message.what = putJoin(getJoinUrl(orderIdString));
                    joinHandler.sendMessage(message);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start());

        DialogInterface.OnClickListener quitClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    @SuppressLint("HandlerLeak") final Handler quitHandler = new Handler() {
                        @Override
                        public void handleMessage(@NonNull Message msg) {
                            switch (msg.what) {
                                case SUCCESS:
                                    Toast.makeText(OneOrderCaseActivity.this, "您已成功退出该小组", Toast.LENGTH_SHORT).show();

                                    // To close websocket, remove tip
                                    Intent quitIntent = new Intent();
                                    quitIntent.putExtra("quitGrpId", orderIdString);
                                    Log.e("quitOrder", "quitGrpId =" + orderIdString);
                                    quitIntent.setAction(group_quit_signal);
                                    sendBroadcast(quitIntent);
                                    break;
                                case ERROR:
                                    Toast.makeText(OneOrderCaseActivity.this, "遇到未知错误，请稍后再试", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(OneOrderCaseActivity.this, "退出小组失败，请稍后再试", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    };
                    new Thread(() -> {
                        try {
                            Message message = new Message();
                            message.what = delQuitOrder(getQuitUrl(orderIdString, idContent));
                            quitHandler.sendMessage(message);
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }).start();

                    setResult(RESULT_OK, null);
                    finish();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
            }
        };

        final AlertDialog.Builder quitBuilder = new AlertDialog.Builder(this);
        quitBuilder.setTitle("确认");
        quitBuilder.setMessage("确定离开吗？")
                .setPositiveButton("确定", quitClickListener)
                .setNegativeButton("我再想想", quitClickListener);

        btnLeave.setOnClickListener(v -> quitBuilder.show());

        DialogInterface.OnClickListener deleteClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    new Thread(new Runnable() {
                        @SuppressLint("HandlerLeak")
                        final Handler deleteHandler = new Handler() {
                            @Override
                            public void handleMessage(@NonNull Message msg) {
                                if (msg.what == SUCCESS) {
                                    Toast.makeText(OneOrderCaseActivity.this, "解散成功！", Toast.LENGTH_SHORT).show();

                                    // To close websocket, remove tip
                                    Intent delIntent = new Intent();
                                    delIntent.putExtra("quitGrpId", orderIdString);
                                    Log.e("delOrder", "quitGrpId =" + orderIdString);
                                    delIntent.setAction(group_quit_signal);
                                    sendBroadcast(delIntent);
                                } else {
                                    Toast.makeText(OneOrderCaseActivity.this, "解散失败！", Toast.LENGTH_SHORT).show();
                                }
                            }
                        };

                        @Override
                        public void run() {
                            try {
                                Message message = new Message();
                                message.what = delDisbandOrder(getDelOrderUrl(orderIdString));
                                deleteHandler.sendMessage(message);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                    setResult(RESULT_OK, null);
                    finish();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
            }
        };

        final AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(this);
        deleteBuilder.setTitle("确认");
        deleteBuilder.setMessage("确定解散吗？")
                .setPositiveButton("确定", deleteClickListener)
                .setNegativeButton("我再想想", deleteClickListener);

        btnDelete.setOnClickListener(v -> deleteBuilder.show());

        ivBack.setOnClickListener(v -> finish());
    }

    private int postGetImageUrl() throws IOException, JSONException {
        final String TAG = "postGetImageUrl";
        int ret;

        final Request request = new Request.Builder()
                .url(getImageDownloadUrl(orderIdString))
                .header("token", idContent + "_" + tokenContent)
                .post(RequestBody.create(null, ""))
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            JSONObject responseJSON = new JSONObject(Objects.requireNonNull(response.body()).string());
            imageUrl = responseJSON.getString("content");
            ret = SUCCESS;
        } else {
            ret = ERROR;
        }

        return ret;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int postQueryGrpMem(String url) throws IOException, JSONException {
        final String TAG = "postQueryGrpMem";
        int ret;

        final Request request = new Request.Builder()
                .url(url)
                .header("token", idContent + "_" + tokenContent)
                .post(RequestBody.create(null, ""))
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            JSONObject responseJSON = new JSONObject(Objects.requireNonNull(response.body()).string());
            String codeString = responseJSON.getString("code");
            int code = Integer.parseInt(codeString);
            if (code == 100) {
                ret = SUCCESS;
                JSONArray contentArray = new JSONArray(responseJSON.getString("content"));
                for (int i = 0; i < contentArray.length(); i++) {
                    String userId = contentArray.getJSONObject(i).getString("userId");
                    String nickname = contentArray.getJSONObject(i).getString("nickname");
                    String memberHeadUrl = contentArray.getJSONObject(i).getString("url");
                    String credit = contentArray.getJSONObject(i).getString("point");
                    String path = getDownloadPath(memberHeadUrl, userId);
                    BriefMemberInfo briefMemberInfo = new BriefMemberInfo(nickname, credit, userId, path);
                    Log.e(TAG, briefMemberInfo.toString());

                    memberIds.add(userId);
                    memberInfoList.add(briefMemberInfo);
                }
            } else {
                ret = FAILED;
            }
        } else {
            ret = ERROR;
        }
        return ret;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int delQuitOrder(String url) throws IOException, JSONException {
        final String TAG = "delQuitOrder";
        int ret = 0;

        final Request request = new Request.Builder()
                .url(url)
                .header("token", idContent + "_" + tokenContent)
                .delete()
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            JSONObject responseJSON = new JSONObject(Objects.requireNonNull(response.body()).string());
            String codeString = responseJSON.getString("code");
            int code = Integer.parseInt(codeString);
            if (code == 100) {
                ret = 1;
            } else {
                ret = 2;
            }
        } else {
            ret = -1;
        }
        return ret;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int putJoin(String url) throws IOException, JSONException {
        final String TAG = "putJoin";
        int ret;

        final Request request = new Request.Builder()
                .url(url)
                .header("token", idContent + "_" + tokenContent)
                .put(RequestBody.create(null, ""))
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        JSONObject responseJSON = new JSONObject(Objects.requireNonNull(response.body()).string());
        String codeString = responseJSON.getString("code");
        int code = Integer.parseInt(codeString);

        if (code == 100) {
            ret = 1;
        } else if (code == -1009) {
            ret = 2;
        } else {
            ret = 3;
            Log.d(TAG, "responseContent: " + responseJSON.toString());
        }

        return ret;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int delDisbandOrder(String url) throws IOException {
        final String TAG = "delOrder";
        int ret = 0;

        final Request request = new Request.Builder()
                .url(url)
                .header("token", idContent + "_" + tokenContent)
                .delete()
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            ret = 1;
        } else {
            Log.d(TAG, Objects.requireNonNull(response.body()).string());
            Log.d(TAG, response.toString());
        }

        return ret;
    }
}
