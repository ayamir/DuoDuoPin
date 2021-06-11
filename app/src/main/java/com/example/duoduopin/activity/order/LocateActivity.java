package com.example.duoduopin.activity.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.duoduopin.R;
import com.example.duoduopin.activity.AssistantLocationActivity;

import java.util.HashMap;
import java.util.Map;

public class LocateActivity extends AppCompatActivity {
    private final HashMap<String, String> addressMap = new HashMap<>();
    private EditText etAddress;

    private Intent setReturnIntent() {
        Intent intent = new Intent();
        String address = etAddress.getText().toString();
        intent.putExtra("address", address);
        putTude(address, intent);
        return intent;
    }

    private void init() {
        addressMap.put("北京邮电大学西门", "39.961075,116.355332");
        addressMap.put("北京邮电大学学生公寓四号楼", "39.962847,116.356687");
        addressMap.put("漫咖啡", "39.963710,116.357495");
        addressMap.put("北京邮电大学东门", "39.962421,116.361011");
        addressMap.put("北京邮电大学科研楼", "39.964399,116.359094");
        addressMap.put("北京邮电大学南门", "39.958218,116.357939");
    }

    private void putTude(String address, Intent intent) {
        for (Map.Entry<String, String> entry : addressMap.entrySet()) {
            if (entry.getKey().equals(address)) {
                intent.putExtra("tude", entry.getValue());
                intent.putExtra("isChosen", true);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        setResult(RESULT_OK, setReturnIntent());
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);

        init();

        ImageView ivLocateBack = findViewById(R.id.iv_locate_back);
        ivLocateBack.setOnClickListener(v -> {
            setResult(RESULT_OK, setReturnIntent());
            finish();
        });

        TextView tvLocation = findViewById(R.id.tv_locate);
        tvLocation.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AssistantLocationActivity.class);
            startActivity(intent);
        });

        etAddress = findViewById(R.id.et_address);

        LinearLayout llXimen = findViewById(R.id.ll_ximen);
        TextView tvXimen = findViewById(R.id.tv_ximen);
        llXimen.setOnClickListener(v -> {
            etAddress.setText(tvXimen.getText());
        });

        LinearLayout llXuesi = findViewById(R.id.ll_xuesi);
        TextView tvXuesi = findViewById(R.id.tv_xuesi);
        llXuesi.setOnClickListener(v -> {
            etAddress.setText(tvXuesi.getText());
        });

        LinearLayout llMancoffee = findViewById(R.id.ll_mancoffee);
        TextView tvMancoffee = findViewById(R.id.tv_mancoffee);
        llMancoffee.setOnClickListener(v -> {
            etAddress.setText(tvMancoffee.getText());
        });

        LinearLayout llDongmen = findViewById(R.id.ll_dongmen);
        TextView tvDongmen = findViewById(R.id.tv_dongmen);
        llDongmen.setOnClickListener(v -> {
            etAddress.setText(tvDongmen.getText());
        });

        LinearLayout llNanmen = findViewById(R.id.ll_nanmen);
        TextView tvNanmen = findViewById(R.id.tv_nanmen);
        llNanmen.setOnClickListener(v -> {
            etAddress.setText(tvNanmen.getText());
        });

        LinearLayout llKeyanlou = findViewById(R.id.ll_keyanlou);
        TextView tvKeyanlou = findViewById(R.id.tv_keyanlou);
        llKeyanlou.setOnClickListener(v -> {
            etAddress.setText(tvKeyanlou.getText());
        });
    }
}
