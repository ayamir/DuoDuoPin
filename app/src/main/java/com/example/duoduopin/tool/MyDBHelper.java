package com.example.duoduopin.tool;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MyDBHelper extends SQLiteOpenHelper {

    private Context mContext;

    public MyDBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SYS_MSG_TBLE = "create table SysMsg ("
                + "messageId integer not null primary key autoincrement, "
                + "senderId integer not null, "
                + "receiverId integer, "
                + "billId integer not null, "
                + "type text not null, "
                + "time text not null, "
                + "content text not null)";
        db.execSQL(CREATE_SYS_MSG_TBLE);
        Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists SysMsg");
        onCreate(db);
    }
}
