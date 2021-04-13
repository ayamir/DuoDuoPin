package com.example.duoduopin.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.example.duoduopin.R;
import com.example.duoduopin.activity.OrderCaseActivity;
import com.example.duoduopin.handler.GeneralMsgHandler;
import com.example.duoduopin.tool.MyDBHelper;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.duoduopin.activity.MainActivity.client;
import static com.example.duoduopin.activity.MainActivity.idContent;
import static com.example.duoduopin.activity.MainActivity.nicknameContent;
import static com.example.duoduopin.activity.MainActivity.prefs;
import static com.example.duoduopin.activity.MainActivity.recBriefOrderContentList;
import static com.example.duoduopin.activity.MainActivity.recOrderContentList;
import static com.example.duoduopin.activity.MainActivity.tokenContent;
import static com.example.duoduopin.handler.GeneralMsgHandler.ERROR;
import static com.example.duoduopin.handler.GeneralMsgHandler.LOGOUT;
import static com.example.duoduopin.handler.GeneralMsgHandler.SUCCESS;
import static com.example.duoduopin.tool.Constants.logoutUrl;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_profile, null);
        return view;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (this.getView() != null) {
            this.getView().setVisibility(menuVisible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        final GeneralMsgHandler myMsgHandler = new GeneralMsgHandler(getActivity());

        super.onActivityCreated(savedInstanceState);

        Button logout = Objects.requireNonNull(getActivity()).findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CommitPrefEdits")
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Message message = new Message();
                            message.arg1 = LOGOUT;
                            int state = delRequest();
                            if (state == SUCCESS) {
                                message.what = SUCCESS;
                            } else {
                                message.what = ERROR;
                            }
                            myMsgHandler.sendMessage(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                cleanPrefs();
                getActivity().finishAndRemoveTask();
            }
        });

        TextView nickname = Objects.requireNonNull(getActivity()).findViewById(R.id.nicknameProfile);
        nickname.setText(nicknameContent);

        LinearLayout carCaseLayout = getActivity().findViewById(R.id.car_case_layout);
        carCaseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), OrderCaseActivity.class);
                intent.putExtra("from", "userId");
                intent.putExtra("type", "CAR");
                intent.putExtra("userId", idContent);
                startActivity(intent);
            }
        });

        LinearLayout orderCaseLayout = getActivity().findViewById(R.id.order_case_layout);
        orderCaseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), OrderCaseActivity.class);
                intent.putExtra("from", "userId");
                intent.putExtra("type", "BILL");
                intent.putExtra("userId", idContent);
                startActivity(intent);
            }
        });

        DialogInterface.OnClickListener clearListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        final MyDBHelper clearStorage = new MyDBHelper(getActivity(), "DuoDuoPin.db", null, 1);
                        SQLiteDatabase db = clearStorage.getWritableDatabase();
                        clearStorage.dropTables(db);
                        Toast.makeText(getActivity(), "清除成功！", Toast.LENGTH_SHORT).show();
                        clearStorage.onCreate(db);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        final AlertDialog.Builder clearBuilder = new AlertDialog.Builder(getActivity());
        clearBuilder.setTitle("确定清除吗？");
        clearBuilder.setMessage("你会失去保存在本地的消息记录！")
                .setPositiveButton("确定", clearListener)
                .setNegativeButton("取消", clearListener);

        LinearLayout clearStorage = getActivity().findViewById(R.id.clear_storage_layout);
        clearStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearBuilder.show();
            }
        });

    }

    private void cleanPrefs() {
        @SuppressLint("CommitPrefEdits")
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("id");
        editor.remove("token");
        editor.remove("lastOnlineTime");
        editor.apply();

        recBriefOrderContentList.clear();
        recOrderContentList.clear();
        Log.e("logout", "cleanPrefs() executed!");
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private int delRequest() throws IOException {
        int ret;
        final String TAG = "logout";

        final Request request = new Request.Builder()
                .url(logoutUrl)
                .header("token", idContent + "_" + tokenContent)
                .delete()
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            ret = SUCCESS;
        } else {
            Log.d(TAG, Objects.requireNonNull(response.body()).string());
            Log.d(TAG, response.toString());
            ret = ERROR;
        }
        return ret;
    }
}
