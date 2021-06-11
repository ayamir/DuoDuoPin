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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.duoduopin.R;
import com.example.duoduopin.adapter.OrderTabAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.activity.MainActivity.client;
import static com.example.duoduopin.activity.MainActivity.idContent;
import static com.example.duoduopin.activity.MainActivity.tokenContent;
import static com.example.duoduopin.handler.GeneralMsgHandler.ERROR;
import static com.example.duoduopin.handler.GeneralMsgHandler.GROUP_FULL;
import static com.example.duoduopin.handler.GeneralMsgHandler.JOIN_REPEAT;
import static com.example.duoduopin.handler.GeneralMsgHandler.SUCCESS;
import static com.example.duoduopin.tool.Constants.getDelOrderUrl;
import static com.example.duoduopin.tool.Constants.getJoinUrl;
import static com.example.duoduopin.tool.Constants.getQueryMemberUrl;
import static com.example.duoduopin.tool.Constants.getQuitUrl;
import static com.example.duoduopin.tool.Constants.group_quit_signal;

public class OneOrderCaseActivity extends FragmentActivity {
    private final int MAIN_POS = 0;
    private final int DETAILS_POS = 1;
    private final int MEMBER_POS = 2;

    private final String MAIN_TAB_CONTENT = "拼单内容";
    private final String DETAILS_TAB_CONTENT = "拼单详情";
    private final String MEMBER_TAB_CONTENT = "拼单成员";

    private final ArrayList<String> memberIds = new ArrayList<>();
    private final ArrayList<String> memberNicknameList = new ArrayList<>();
    private final ArrayList<String> memberCreditList = new ArrayList<>();

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
    private String headPath;
    private String creditString;
    private Button btnDelete, btnJoin, btnLeave;
    private ImageView ivBack;
    private ImageView ivItemPic;
    private TextView tvItemComment;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_order_case);
        getInfoFromIntent();
        try {
            bindItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
//        if (imageUrl.isEmpty()) {
//            ivItemPic.setVisibility(View.INVISIBLE);
//            tvItemComment.setVisibility(View.INVISIBLE);
//        }

        @SuppressLint("HandlerLeak") final Handler isInHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == SUCCESS) {
                    if (isInMembers()) {
                        if (userIdString.equals(idContent))
                            btnLeave.setVisibility(View.INVISIBLE);
                        btnJoin.setVisibility(View.INVISIBLE);
                    } else {
                        btnLeave.setVisibility(View.INVISIBLE);
                        btnJoin.setVisibility(View.VISIBLE);
                    }
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

    }

    private boolean isInMembers() {
        return memberIds.contains(idContent);
    }

    private void getInfoFromIntent() {
        Intent fromIntent = getIntent();
        if (fromIntent != null) {
            orderIdString = fromIntent.getStringExtra("orderId");
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
        bundle.putString("imageUrl", imageUrl);
        bundle.putString("price", priceString);
        bundle.putString("title", titleString);
        bundle.putString("description", descriptionString);
        bundle.putString("headPath", headPath);
        bundle.putString("nickname", nicknameString);
        bundle.putString("credit", creditString);

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
        bundle.putStringArrayList("nicknameList", memberNicknameList);
        bundle.putStringArrayList("creditList", memberCreditList);
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

    private void bindItems() throws IOException {
        tabAdapter.setBundle(setMainBundle(), MAIN_POS);
        tabAdapter.setBundle(setDetailsBundle(), DETAILS_POS);
        tabAdapter.setBundle(setMemberBundle(), MEMBER_POS);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager2 = findViewById(R.id.viewpager2);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
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

//        ivItemPic = findViewById(R.id.iv_item_pic);
//        tvItemComment = findViewById(R.id.tv_item_comment);
//        if (!imageUrl.isEmpty()) {
//            Bitmap bitmap = BitmapFactory.decodeStream(new URL(imageUrl).openStream());
//            ivItemPic.setImageBitmap(bitmap);
//        }
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
                            @SuppressLint("HandlerLeak")
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int postQueryGrpMem(String url) throws IOException, JSONException {
        final String TAG = "postQueryGrpMem";
        int ret = 0;

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
                ret = 1;
                JSONArray contentArray = new JSONArray(responseJSON.getString("content"));
                for (int i = 0; i < contentArray.length(); i++) {
                    memberIds.add(contentArray.getJSONObject(i).getString("userId"));
                }
            } else {
                ret = 2;
            }
        } else {
            ret = -1;
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
