package com.example.duoduopin.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.duoduopin.R;
import com.example.duoduopin.activity.LoginActivity;
import com.example.duoduopin.activity.OrderCaseActivity;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.duoduopin.activity.LoginActivity.idContent;
import static com.example.duoduopin.activity.LoginActivity.nicknameContent;
import static com.example.duoduopin.activity.LoginActivity.tokenContent;
import static com.example.duoduopin.activity.MainActivity.client;
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
        super.onActivityCreated(savedInstanceState);

        Button logout = Objects.requireNonNull(getActivity()).findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CommitPrefEdits")
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                try {
                    delRequest();
                    Intent intent = new Intent(v.getContext(), LoginActivity.class);
                    intent.putExtra("logout", "true");
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        TextView nickname = Objects.requireNonNull(getActivity()).findViewById(R.id.nicknameProfile);
        nickname.setText(nicknameContent);

        TextView myCarText = getActivity().findViewById(R.id.myCarText);
        myCarText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), OrderCaseActivity.class);
                intent.putExtra("from", "userId");
                intent.putExtra("type", "CAR");
                intent.putExtra("userId", idContent);
                startActivity(intent);
            }
        });
        ImageView myCarImage = getActivity().findViewById(R.id.myCarImage);
        myCarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), OrderCaseActivity.class);
                intent.putExtra("from", "userId");
                intent.putExtra("type", "CAR");
                intent.putExtra("userId", idContent);
                startActivity(intent);
            }
        });

        TextView myOrderText = getActivity().findViewById(R.id.myOrderText);
        myOrderText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), OrderCaseActivity.class);
                intent.putExtra("from", "userId");
                intent.putExtra("type", "BILL");
                intent.putExtra("userId", idContent);
                startActivity(intent);
            }
        });
        ImageView myOrderImage = getActivity().findViewById(R.id.myOrderImage);
        myOrderImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), OrderCaseActivity.class);
                intent.putExtra("from", "userId");
                intent.putExtra("type", "BILL");
                intent.putExtra("userId", idContent);
                startActivity(intent);
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void delRequest() throws IOException {
        final String TAG = "logout";

        final Request request = new Request.Builder()
                .url(logoutUrl)
                .header("token", idContent + "_" + tokenContent)
                .delete()
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            Toast.makeText(getActivity(), "登出成功，请重新登录", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, Objects.requireNonNull(response.body()).string());
            Log.d(TAG,  response.toString());
        }
    }
}
