package com.vibexie.jianai.Utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.vibexie.jianai.Dao.DBManager.DBManager;

import java.util.ArrayList;

/**
 * 这是一个在sqliteDatabase获取刷新数据的管理类，主要用于下拉刷新指定条数的记录
 * Created by vibexie on 5/12/15.
 */
public class FreshDatasManager<T> {
    /**
     * 声明一个泛型对象，在cursor2Beans()方法中需要使用
     */
    T t;

    private SQLiteDatabase sqLiteDatabase;

    /**
     * 已经获取了hasLoadAfterThis行之后的数据
     */
    private int hasLoadAfterThis;

    /**
     * 每组数据的大小（行数）
     */
    private int GROUPSIZE;

    /**
     * 构造方法
     * @param sqLiteDatabase
     * @param t     要读取的bean的一个对象，主要是为了反射
     * @param tableName 表名
     * @param groupSize 每次读取的记录数
     */
    public FreshDatasManager(SQLiteDatabase sqLiteDatabase,T t,String tableName,int groupSize){
        this.sqLiteDatabase=sqLiteDatabase;
        this.t=t;
        this.GROUPSIZE=groupSize;

        Cursor cursor=sqLiteDatabase.rawQuery("select count(*) from "+tableName+";",null);
        cursor.moveToFirst();
        cursor.getInt(0);

        /**
         * hasLoadAfterThis的大小就是记录的总数，又因为行是从0开始计数，所以还得-1
         */
        hasLoadAfterThis=cursor.getInt(0)-1;
    }

    /**
     * 获取GROUPSIZE行刷新数据
     * @return 若返回空，则说明已经刷新了所有的数据
     */
    public ArrayList<T> getRefreshBeans(){
        ArrayList<T> list=new ArrayList<T>();
        Cursor cursor;

        if(hasLoadAfterThis>GROUPSIZE-1){
            hasLoadAfterThis-=(GROUPSIZE-1);
            cursor=sqLiteDatabase.rawQuery("select * from friend_msg limit " + hasLoadAfterThis + "," + GROUPSIZE +";",null);
            hasLoadAfterThis-=1;

        }else if(hasLoadAfterThis>=0){
            /*获取最后一组数据*/

            cursor=sqLiteDatabase.rawQuery("select * from friend_msg limit 0,"+(hasLoadAfterThis+1)+";",null);

            /**
             * 标记没有数据了
             */
            hasLoadAfterThis=-1;

        }else {

            /*已经获取完了数据*/
            return null;
        }

        /**
         * 需要的话可以把DBManager.cursor2Beans()的代码剪切到这个类中
         */
        list=DBManager.cursor2Beans(t,cursor);

        return list;
    }
}