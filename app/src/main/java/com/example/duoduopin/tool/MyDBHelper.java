package com.example.duoduopin.tool;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MyDBHelper extends SQLiteOpenHelper {

    public MyDBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public String CREATE_SYS_MSG_TABLE = "create table SysMsg ("
            + "messageId integer not null primary key, "
            + "senderId integer not null, "
            + "receiverId integer, "
            + "billId integer not null, "
            + "type text not null, "
            + "time text not null, "
            + "content text not null)";

    // ownerId for multiple users' support
    public String CREATE_GRP_MSG_TABLE = "create table GrpMsg ("
            + "groupId integer not null, "
            + "groupTitle text not null, "
            + "ownerId integer not null, "
            + "userId integer not null, "
            + "nickname text not null, "
            + "content text not null, "
            + "time text not null, "
            + "primary key (groupId, userId, time))"
            ;


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SYS_MSG_TABLE);
        db.execSQL(CREATE_GRP_MSG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists SysMsg");
        db.execSQL("drop table if exists GrpMsg");
        onCreate(db);
    }
}
