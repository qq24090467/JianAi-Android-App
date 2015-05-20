package com.vibexie.jianai.Dao.DBHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * 一个用户的数据库
 * Created by vibexie on 4/29/15.
 */
public class UserDBHelper extends SQLiteOpenHelper{
    private static int DATABASE_VERSION = 1;

    /**
     * 用户聊天信息表,用户保存用户的聊天记录
     */
    private static String CHAT_MSG_TABLE_CREATE_SQL = "create table friend_msg(_id integer primary key autoincrement,msgTime varchar(20),msgContent text,fromOrTo tinyint,read tinyint);";

    /**
     * 系统通知表，用户保存用户接收到的系统通知
     */
    private static String SERVER_MSG_TABLE_CREATE_SQL = "create table server_msg(_id integer primary key autoincrement,msgTime varchar(20),msgContent text,fromOrTo tinyint);";

    /**
     * 用户信息表
     */
    private static String USER_INFO_TABLE_CREATE_SQL= "create table user_info(_id integer primary key autoincrement," +
            "id int(11)," +
            "username varchar(64)," +
            "loverName varchar(64)," +
            "plainPassword varchar(32)," +
            "encryptedPassword varchar(255)," +
            "name varchar(100)," +
            "sex varchar(5)," +
            "birthday varchar(20)," +
            "email varchar(100)," +
            "phone varchar(50)," +
            "addr varchar(100)," +
            "sign varchar(100)," +
            "headimage mediumblob," +
            "creationDate char(15)," +
            "modificationDate char(15));";

    /**
     * 构造方法
     * @param context
     * @param databaseName  数据库名，这里的数据库名格式为：user_id.db，如1000001.db
     */
    public UserDBHelper(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CHAT_MSG_TABLE_CREATE_SQL);
        db.execSQL(SERVER_MSG_TABLE_CREATE_SQL);
        db.execSQL(USER_INFO_TABLE_CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}