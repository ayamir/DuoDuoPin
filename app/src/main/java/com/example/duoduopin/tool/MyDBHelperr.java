package com.example.duoduopin.tool;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MyDBHelperr extends SQLiteOpenHelper {
    private final String CREATE_SYS_MSG_TBLE = "create table SysMsg ("
            + "messageId integer primary key autoincrement, "
            + "senderId integer, "
            + "receiverId integer, "
            + "billId integer, "
            + "type text, "
            + "time text, "
            + "content text)";

    private Context mContext;

    public MyDBHelperr(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SYS_MSG_TBLE);
        Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists SysMsg");
        onCreate(db);
    }
}
