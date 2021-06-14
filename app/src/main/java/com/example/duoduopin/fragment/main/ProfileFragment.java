package com.example.duoduopin.fragment.main;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.example.duoduopin.R;
import com.example.duoduopin.activity.order.OrderCaseActivity;
import com.example.duoduopin.activity.profile.EditUserInfoActivity;
import com.example.duoduopin.handler.GeneralMsgHandler;
import com.example.duoduopin.tool.MyDBHelper;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.duoduopin.activity.MainActivity.client;
import static com.example.duoduopin.activity.MainActivity.creditContent;
import static com.example.duoduopin.activity.MainActivity.head;
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
    public static final int EDIT_USER_INFO_REQUEST = 4000;
    public static final int EDIT_USER_NICKNAME_REQUEST = 4001;
    public static final int EDIT_USER_HEAD_REQUEST = 4002;
    private TextView tvProfileNickname;

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_USER_INFO_REQUEST) {
            if (resultCode == SUCCESS) {
                if (data != null) {
                    boolean isNicknameChanged = data.getBooleanExtra("isNicknameChanged", false);
                    if (isNicknameChanged) {
                        nicknameContent = data.getStringExtra("nickname");
                        tvProfileNickname.setText(nicknameContent);
                    }
                    boolean isHeadChanged = data.getBooleanExtra("isHeadChanged", false);
                    if (isHeadChanged) {
                        String headPath = data.getStringExtra("headPath");
                        // TODO: handle headPath
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        final GeneralMsgHandler myMsgHandler = new GeneralMsgHandler(getActivity());

        super.onActivityCreated(savedInstanceState);

        TextView tvProfileEdit = getActivity().findViewById(R.id.tv_profile_edit);
        tvProfileEdit.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditUserInfoActivity.class);
            // TODO: Add credit
            intent.putExtra("credit", "");
            startActivityForResult(intent, EDIT_USER_INFO_REQUEST);
        });

        ImageView ivUserHead = getActivity().findViewById(R.id.iv_profile_user_head);
        ivUserHead.setImageBitmap(head);

        tvProfileNickname = Objects.requireNonNull(getActivity()).findViewById(R.id.tv_profile_nickname);
        tvProfileNickname.setText(nicknameContent);

        TextView tvProfileUserCredit = Objects.requireNonNull(getActivity().findViewById(R.id.tv_profile_user_credit));
        tvProfileUserCredit.setText(creditContent);


        LinearLayout carCaseLayout = getActivity().findViewById(R.id.car_case_layout);
        carCaseLayout.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), OrderCaseActivity.class);
            intent.putExtra("from", "userId");
            intent.putExtra("type", "CAR");
            intent.putExtra("userId", idContent);
            startActivity(intent);
        });

        LinearLayout orderCaseLayout = getActivity().findViewById(R.id.order_case_layout);
        orderCaseLayout.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), OrderCaseActivity.class);
            intent.putExtra("from", "userId");
            intent.putExtra("type", "BILL");
            intent.putExtra("userId", idContent);
            startActivity(intent);
        });

        DialogInterface.OnClickListener clearListener = (dialog, which) -> {
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
        };

        final AlertDialog.Builder clearBuilder = new AlertDialog.Builder(getActivity());
        clearBuilder.setTitle("确定清除吗？");
        clearBuilder.setMessage("你会失去保存在本地的消息记录！")
                .setPositiveButton("确定", clearListener)
                .setNegativeButton("取消", clearListener);

        LinearLayout clearStorage = getActivity().findViewById(R.id.clear_storage_layout);
        clearStorage.setOnClickListener(v -> clearBuilder.show());

        Button logout = Objects.requireNonNull(getActivity()).findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
            new Thread(() -> {
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
            }).start();
            cleanPrefs();
            getActivity().finishAndRemoveTask();
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
