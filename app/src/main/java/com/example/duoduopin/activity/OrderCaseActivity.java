package com.example.duoduopin.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.duoduopin.R;
import com.example.duoduopin.bean.ContentBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.activity.LoginActivity.JSON;
import static com.example.duoduopin.activity.LoginActivity.idContent;
import static com.example.duoduopin.activity.LoginActivity.tokenContent;

public class OrderCaseActivity extends AppCompatActivity {
    private String type;
    private ListView listView;

    private List<ContentBean> orderContent;
    private final ArrayList<HashMap<String, String>> cases = new ArrayList<>();
    private final ArrayList<HashMap<String, String>> detailCases = new ArrayList<>();

    private String timeStart, timeEnd, minPrice, maxPrice, description, orderType, distance, longitude, latitude;

    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_case);

        listView = findViewById(R.id.orderCase);

        Intent fromIntent = getIntent();
        assert fromIntent != null;
        String from = fromIntent.getStringExtra("from");
        type = fromIntent.getStringExtra("type");
        boolean isInfo = false;

        String queryUrl = "http://123.57.12.189:8080/ShareBill/";
        switch (from) {
            case "userId":
                String userId = fromIntent.getStringExtra("userId");
                try {
                    int state = postRequest(queryUrl + "user/" + userId, isInfo);
                    if (state == 1) {
                        showItems();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "orderId":
                String orderId = fromIntent.getStringExtra("orderId");
                try {
                    int state = postRequest(queryUrl + orderId, isInfo);
                    if (state == 1) {
                        showItems();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "info":
                isInfo = true;
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
                try {
                    int state = postRequest(queryUrl + "info", isInfo);
                    if (state == 1) {
                        showItems();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                break;
        }

    }

    private void fillCases(ContentBean content) {
        HashMap<String, String> map = new HashMap<>();
        map.put("title", content.getTitle());
        map.put("description", content.getDescription());
        map.put("curNumContent", content.getCurPeople());
        cases.add(map);

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
        detailCases.add(dMap);
    }

    private void showItems() {
        for (ContentBean content : orderContent) {
            if (type.equals("all")) {
                fillCases(content);
            } else {
                if (content.getType().equals(type)) {
                    fillCases(content);
                }
            }
        }

        SimpleAdapter adapter = new SimpleAdapter(this, cases, R.layout.one_item,
                new String[]{"title", "description", "curNumContent"},
                new int[]{R.id.title, R.id.description, R.id.curNumContent});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent toIntent = new Intent(view.getContext(), OneCaseActivity.class);
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
                startActivity(toIntent);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int postRequest(String url, boolean isInfo) throws IOException, JSONException {
        int ret = 0;

        Request request;

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

        String TAG = "queryOrderCase";
        if (response.code() == 200) {
            JSONObject responseJson = new JSONObject(Objects.requireNonNull(response.body()).string());
            orderContent = new Gson().fromJson(responseJson.getString("content"), new TypeToken<List<ContentBean>>() {
            }.getType());
            Log.d(TAG, "postRequest: " + orderContent.toString());
            if (orderContent != null) {
                ret = 1;
            }
        } else {
            Log.d(TAG, Objects.requireNonNull(response.body()).string());
            Log.d(TAG, response.toString());
        }

        return ret;
    }
}