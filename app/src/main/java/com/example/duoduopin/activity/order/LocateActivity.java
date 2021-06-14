package com.example.duoduopin.activity.order;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
        addressMap.put("北京邮电大学西门", "116.355332,39.961075");
        addressMap.put("北京邮电大学学生公寓四号楼", "116.356687,39.962847");
        addressMap.put("漫咖啡", "116.357495,39.963710");
        addressMap.put("北京邮电大学东门", "116.361011,39.962421");
        addressMap.put("北京邮电大学科研楼", "116.359094,39.964399");
        addressMap.put("北京邮电大学南门", "116.357939,39.958218");
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
        Log.e("KeyDown", "keydown pressed");
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
            Log.e("back", "keydown pressed");
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
