package com.example.duoduopin.activity.order;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.duoduopin.R;
import com.example.duoduopin.pojo.OrderContent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.activity.LoginActivity.JSON;
import static com.example.duoduopin.activity.MainActivity.client;
import static com.example.duoduopin.activity.MainActivity.idContent;
import static com.example.duoduopin.activity.MainActivity.tokenContent;
import static com.example.duoduopin.handler.GeneralMsgHandler.ERROR;
import static com.example.duoduopin.handler.GeneralMsgHandler.SUCCESS;
import static com.example.duoduopin.tool.Constants.getQueryUrlByOrderId;
import static com.example.duoduopin.tool.Constants.getQueryUrlByUserId;
import static com.example.duoduopin.tool.Constants.queryByInfoUrl;

public class OrderCaseActivity extends AppCompatActivity {
    private Intent fromIntent;
    private String type;
    private String from;
    private ListView listView;

    private ArrayList<OrderContent> orderContentList;
    private final ArrayList<HashMap<String, String>> cases = new ArrayList<>();
    private final ArrayList<HashMap<String, String>> detailCases = new ArrayList<>();
    private SwipeRefreshLayout orderCaseSwipeRefresh;

    private String timeStart, timeEnd, minPrice, maxPrice, description, orderType, distance, longitude, latitude;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == SUCCESS) {
                showItems();
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_case);

        listView = findViewById(R.id.orderCase);

        fromIntent = getIntent();
        assert fromIntent != null;

        from = fromIntent.getStringExtra("from");
        type = fromIntent.getStringExtra("type");

        switchShow();

        orderCaseSwipeRefresh = findViewById(R.id.order_case_swipe_refresh);
        orderCaseSwipeRefresh.setOnRefreshListener(() -> {
            cases.clear();
            detailCases.clear();
            switchShow();
            orderCaseSwipeRefresh.setRefreshing(false);
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void switchShow() {
        final String TAG = "switchShow";
        switch (from) {
            case "userId":
                final String userId = fromIntent.getStringExtra("userId");
                new Thread(() -> {
                    try {
                        Message message = new Message();
                        int state = postQueryOrder(getQueryUrlByUserId(userId), false);
                        if (state == SUCCESS) {
                            message.what = SUCCESS;
                        } else {
                            message.what = ERROR;
                        }
                        handler.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }).start();
                break;
            case "orderId":
                final String orderId = fromIntent.getStringExtra("orderId");
                new Thread(() -> {
                    try {
                        Message message = new Message();
                        int state = postQueryOrder(getQueryUrlByOrderId(orderId), false);
                        if (state == 1) {
                            message.what = SUCCESS;
                        } else {
                            message.what = ERROR;
                        }
                        handler.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }).start();
                break;
            case "info":
                timeStart = fromIntent.getStringExtra("timeStart");
                if (timeStart.isEmpty())
                    timeStart = null;
                timeEnd = fromIntent.getStringExtra("timeEnd");
                if (timeEnd.isEmpty())
                    timeEnd = null;
                minPrice = fromIntent.getStringExtra("minPrice");
                if (minPrice.isEmpty())
                    minPrice = null;
                maxPrice = fromIntent.getStringExtra("maxPrice");
                if (maxPrice.isEmpty())
                    maxPrice = null;
                description = fromIntent.getStringExtra("description");
                if (description.isEmpty())
                    description = null;
                orderType = fromIntent.getStringExtra("orderType");
                distance = fromIntent.getStringExtra("distance");
                longitude = fromIntent.getStringExtra("longitude");
                latitude = fromIntent.getStringExtra("latitude");
                Log.e("Search By Info", "orderType = " + orderType + ", distance = " + distance + ", longitude = " + longitude + ", latitude = " + latitude);
                new Thread(() -> {
                    try {
                        Message message = new Message();
                        int state = postQueryOrder(queryByInfoUrl, true);
                        if (state == 1) {
                            message.what = SUCCESS;
                        } else {
                            message.what = ERROR;
                        }
                        handler.sendMessage(message);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }).start();
                break;
        }
    }

    private void fillCases(OrderContent content) {
        HashMap<String, String> map = new HashMap<>();
        map.put("title", content.getTitle());
        map.put("description", content.getDescription());
        map.put("curNumContent", content.getCurPeople());
        if (!cases.contains(map)) {
            cases.add(map);
        }

        HashMap<String, String> dMap = new HashMap<>();
        dMap.put("userId", content.getUserId());
        dMap.put("nickname", content.getNickname());
        dMap.put("orderId", content.getBillId());
        dMap.put("type", content.getType());
        dMap.put("price", content.getPrice());
        dMap.put("address", content.getAddress());
        dMap.put("curPeople", content.getCurPeople());
        dMap.put("maxPeople", content.getMaxPeople());
        dMap.put("time", content.getTime());
        dMap.put("description", content.getDescription());
        dMap.put("title", content.getTitle());
        if (!detailCases.contains(dMap)) {
            detailCases.add(dMap);
        }
    }

    private void showItems() {
        for (OrderContent content : orderContentList) {
            if (type.equals("all")) {
                fillCases(content);
            } else {
                if (content.getType().equals(type)) {
                    fillCases(content);
                }
            }
        }

        SimpleAdapter adapter = new SimpleAdapter(this, cases, R.layout.tip_item_order,
                new String[]{"title", "description", "curNumContent"},
                new int[]{R.id.tv_title, R.id.tv_description, R.id.curNumContent});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent toIntent = new Intent(view.getContext(), OneOrderCaseActivity.class);
            toIntent.putExtra("userId", detailCases.get((int) id).get("userId"));
            toIntent.putExtra("nickname", detailCases.get((int) id).get("nickname"));
            toIntent.putExtra("type", detailCases.get((int) id).get("type"));
            toIntent.putExtra("orderId", detailCases.get((int) id).get("orderId"));
            toIntent.putExtra("price", detailCases.get((int) id).get("price"));
            toIntent.putExtra("address", detailCases.get((int) id).get("address"));
            toIntent.putExtra("curPeople", detailCases.get((int) id).get("curPeople"));
            toIntent.putExtra("maxPeople", detailCases.get((int) id).get("maxPeople"));
            toIntent.putExtra("time", detailCases.get((int) id).get("time"));
            toIntent.putExtra("description", detailCases.get((int) id).get("description"));
            toIntent.putExtra("title", detailCases.get((int) id).get("title"));
            startActivityForResult(toIntent, 1);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            cases.clear();
            detailCases.clear();
            switchShow();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private int postQueryOrder(String url, boolean isInfo) throws IOException, JSONException {
        final String TAG = "queryOrderCase";
        Request request;

        int ret = 0;

        if (isInfo) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", orderType);
            jsonObject.put("description", description);
            jsonObject.put("startTime", timeStart);
            jsonObject.put("endTime", timeEnd);
            jsonObject.put("minPrice", minPrice);
            jsonObject.put("maxPrice", maxPrice);
            jsonObject.put("distance", distance);

            jsonObject.put("longitude", longitude);
            jsonObject.put("latitude", latitude);

            Log.d("searchDetail", jsonObject.toString());

            RequestBody body = RequestBody.create(jsonObject.toString(), JSON);

            request = new Request.Builder()
                    .url(url)
                    .header("token", idContent + "_" + tokenContent)
                    .post(body)
                    .build();
        } else {
            request = new Request.Builder()
                    .url(url)
                    .header("token", idContent + "_" + tokenContent)
                    .post(RequestBody.create(null, ""))
                    .build();
        }

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            JSONObject responseJson = new JSONObject(Objects.requireNonNull(response.body()).string());
            orderContentList = new Gson().fromJson(responseJson.getString("content"), new TypeToken<List<OrderContent>>() {
            }.getType());
            if (orderContentList != null) {
                for (OrderContent orderContent : orderContentList) {
                    long oldTime = Long.parseLong(orderContent.getTime());
                    String newTime = Instant.ofEpochMilli(oldTime).atZone(ZoneOffset.ofHours(8)).toLocalDateTime().toString().replace('T', ' ');
                    orderContent.setTime(newTime);
                }
                ret = 1;
            }
        } else {
            Log.d(TAG, Objects.requireNonNull(response.body()).string());
            Log.d(TAG, response.toString());
            ret = -1;
        }
        return ret;
    }
}
