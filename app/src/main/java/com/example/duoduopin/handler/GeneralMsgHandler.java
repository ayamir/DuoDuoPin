package com.example.duoduopin.handler;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class GeneralMsgHandler extends Handler {
    private final Context context;

    public static final int SUCCESS = 1;
    public static final int ERROR = -1;
    public static final int JOIN_REPEAT = 2;
    public static final int GROUP_FULL = 3;

    public static final int REGISTER = 0;
    public static final int LOGIN = 1;
    public static final int LOGOUT = 2;
    public static final int QUERY_ORDER = 3;
    public static final int QUERY_SYS_MSG = 4;
    public static final int QUERY_GRP_MSG = 5;

    public GeneralMsgHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        if (msg.what == SUCCESS) {
            makeToast(context, msg.arg1);
        }
    }

    private void makeToast(Context context, int signal) {
        if (signal == REGISTER) {
            Toast.makeText(context, "注册成功！", Toast.LENGTH_SHORT).show();
        } else if (signal == LOGIN) {
            Toast.makeText(context, "登录成功！", Toast.LENGTH_SHORT).show();
        } else if (signal == LOGOUT) {
            Toast.makeText(context, "登出成功！", Toast.LENGTH_SHORT).show();
        } else if (signal == QUERY_ORDER) {
            Toast.makeText(context, "加载拼单数据成功！", Toast.LENGTH_SHORT).show();
        } else if (signal == QUERY_SYS_MSG) {
            Toast.makeText(context, "加载系统消息成功！", Toast.LENGTH_SHORT).show();
        } else if (signal == QUERY_GRP_MSG) {
            Toast.makeText(context, "加载小组消息成功！", Toast.LENGTH_SHORT).show();
        }
    }
}
