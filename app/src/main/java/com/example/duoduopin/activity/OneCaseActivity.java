package com.example.duoduopin.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.duoduopin.R;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.duoduopin.activity.LoginActivity.tokenContent;
import static com.example.duoduopin.activity.LoginActivity.idContent;
import static com.example.duoduopin.tool.Constants.delOrderUrl;
import static com.example.duoduopin.tool.Constants.getDelOrderUrl;

public class OneCaseActivity extends AppCompatActivity {

    private String userIdString, nicknameString, orderIdString, typeString, priceString, addressString, curPeopleString, maxPeopleString, timeString, descriptionString, titleString;

    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_case);

        Intent fromIntent = getIntent();
        if (fromIntent != null) {
            orderIdString = fromIntent.getStringExtra("orderId");
            if (orderIdString != null) {
                String TAG = "fromIntentDebug";
                Log.d(TAG, orderIdString);

                userIdString = fromIntent.getStringExtra("userId");
                nicknameString = fromIntent.getStringExtra("nickname");
                typeString = fromIntent.getStringExtra("type");
                priceString = fromIntent.getStringExtra("price");
                addressString = fromIntent.getStringExtra("address");
                curPeopleString = fromIntent.getStringExtra("curPeople");
                maxPeopleString = fromIntent.getStringExtra("maxPeople");
                timeString = fromIntent.getStringExtra("time").replace('T', ' ');
                descriptionString = fromIntent.getStringExtra("description");
                titleString = fromIntent.getStringExtra("title");
            }
        }

        Button join = findViewById(R.id.joinButton);
        Button leave = findViewById(R.id.leaveButton);
        Button delete = findViewById(R.id.deleteButton);
        ImageView back = findViewById(R.id.backButton);

        initValue();

        if (userIdString.equals(idContent)) {
            delete.setVisibility(View.VISIBLE);
            leave.setVisibility(View.VISIBLE);
            join.setVisibility(View.INVISIBLE);
        } else {
            delete.setVisibility(View.INVISIBLE);
            leave.setVisibility(View.INVISIBLE);
            join.setVisibility(View.VISIBLE);
        }

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "等待进一步开发...", Toast.LENGTH_SHORT).show();
            }
        });

        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "等待进一步开发...", Toast.LENGTH_SHORT).show();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                int state = 0;
                try {
                    state = delOrder(getDelOrderUrl(orderIdString));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (state == 1) {
                    finish();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int delOrder(String url) throws IOException {
        int ret = 1;

        final Request request = new Request.Builder()
                .url(url)
                .header("token", idContent + "_" + tokenContent)
                .delete()
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        String TAG = "delOrder";
        if (response.code() == 200) {
            Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
            ret = 1;
        } else {
            Log.d(TAG, Objects.requireNonNull(response.body()).string());
            Log.d(TAG, response.toString());
        }

        return ret;
    }

    private void initValue() {
        EditText nickname = findViewById(R.id.nickname);
        EditText userId = findViewById(R.id.userId);
        EditText orderId = findViewById(R.id.orderId);
        EditText type = findViewById(R.id.type);
        EditText price = findViewById(R.id.price);
        EditText address = findViewById(R.id.address);
        EditText curPeople = findViewById(R.id.curNumber);
        EditText maxPeople = findViewById(R.id.maxNumber);
        TextView time = findViewById(R.id.time);
        TextView description = findViewById(R.id.description);
        TextView title = findViewById(R.id.title);

        nickname.setText(nicknameString);
        userId.setText(userIdString);
        type.setText(typeString);
        orderId.setText(orderIdString);
        price.setText(priceString);
        address.setText(addressString);
        curPeople.setText(curPeopleString);
        maxPeople.setText(maxPeopleString);
        time.setText(timeString);
        description.setText(descriptionString);
        title.setText(titleString);
    }
}
