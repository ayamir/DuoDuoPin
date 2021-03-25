package com.example.duoduopin.tool;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MyDBHelper extends SQLiteOpenHelper {

    private final Context mContext;

    public MyDBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SYS_MSG_TABLE = "create table SysMsg ("
                + "messageId integer not null primary key, "
                + "senderId integer not null, "
                + "receiverId integer, "
                + "billId integer not null, "
                + "type text not null, "
                + "time text not null, "
                + "content text not null)";

        // ownerId for multiple users' support
        String CREATE_GRP_MSG_TABLE  = "create table GrpMsg ("
                + "groupId integer not null primary key, "
                + "groupTitle text not null, "
                + "ownerId integer not null, "
                + "userId integer not null, "
                + "nickname text not null, "
                + "content text not null, "
                + "time text not null)"
                ;

        String CREATE_GRP_NEW_MSG_TABLE = "create table GrpNewMsg ("
                + "groupOwnerId text not null primary key, "
                + "groupTitle text not null, "
                + "content text not null, "
                + "time text not null)"
                ;

        db.execSQL(CREATE_SYS_MSG_TABLE);
        db.execSQL(CREATE_GRP_MSG_TABLE);
        db.execSQL(CREATE_GRP_NEW_MSG_TABLE);
        Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists SysMsg");
        onCreate(db);
    }
}
