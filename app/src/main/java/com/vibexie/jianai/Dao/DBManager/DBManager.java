package com.vibexie.jianai.Dao.DBManager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.vibexie.jianai.Dao.DBHelper.UserDBHelper;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * 这里管理类，只提供了CRUD方法，如果需要调用getSqLiteDatabase()获取SQLiteDatabase对象，
 * 再使用SQLiteDatabase原始方法操作数据库
 * 注意:数据表如果有自增长字段，请将字段名设为:_id
 *
 * Created by vibexie on 5/3/15.
 */
public class DBManager {
    private SQLiteDatabase sqLiteDatabase;

    /**
     * 构造方法
     * @param sqLiteOpenHelper 上转型为SQLiteOpenHelper
     */
    public DBManager(SQLiteOpenHelper sqLiteOpenHelper){
        sqLiteDatabase=sqLiteOpenHelper.getWritableDatabase();
    }

    /**
     * 返回SQLiteDatabase对象，当需要使用SQLiteDatabase提供的原始方法时才需要返回
     * @return
     */
    public SQLiteDatabase getSqLiteDatabase(){
        return sqLiteDatabase;
    }

    /**
     * 向指定的表中插入一个bean
     * @param table
     * @param t
     * @param <T>
     * @return
     */
    public <T> boolean insertBean(String table,T t){
        ContentValues contentValues;

        if((contentValues=bean2ContentValues(t))==null){
            /*转化失败*/

            return false;
        }

        /**
         * _id字段为自增长，所以去除
         */
        if(contentValues.containsKey("_id")){
            contentValues.remove("_id");
        }

        if(sqLiteDatabase.insertWithOnConflict(table,null,contentValues,SQLiteDatabase.CONFLICT_ROLLBACK)!=-1){
            /*写入成功*/

            return true;
        }

        return false;
    }

    /**
     *删除bean
     * @param table
     * @param whereClause   如："username=?,password=?"
     * @param whereArgs     参数,即？号，如："{"vibexie","123456"}"
     * @return
     */
    public boolean deleteBean(String table, String whereClause, String[] whereArgs){

        if(sqLiteDatabase.delete(table,whereClause,whereArgs)>0){
            return true;
        }

        return false;
    }

    /**
     * 删除数据库 api16++
     * @param fileName
     * @return
     */
    public boolean deleteDatabase(String fileName){
        File file=new File(fileName);
        return SQLiteDatabase.deleteDatabase(file);
    }

    /**
     * 修改bean
     * @param t
     * @param table
     * @param whereClause
     * @param whereArgs
     * @param <T>
     * @return
     */
    public <T> boolean updateBean(T t,String table, String whereClause, String[] whereArgs){

        ContentValues contentValues=bean2ContentValues(t);

        /**
         * _id字段为自增长，所以去除
         */
        if(contentValues.containsKey("_id")){
            contentValues.remove("_id");
        }

        if(sqLiteDatabase.updateWithOnConflict(table,contentValues,whereClause,whereArgs,SQLiteDatabase.CONFLICT_ROLLBACK)>0){
            return true;
        }

        return false;
    }

    /**
     * 获取bean对象数组
     * @param t         bean对象，仅仅为了反射
     * @param table     表名
     * @param selection 查寻的条件，如："username=?,password=?"
     * @param selectionArgs 条件的参数,即？号，如："{"vibexie","123456"}"
     * @return  返回一个bean对象数组
     */
    public <T> ArrayList<T> getBeans(T t,String table,String selection, String[] selectionArgs){

        Cursor cursor=sqLiteDatabase.query(table,null,selection,selectionArgs,null,null,null);

        return cursor2Beans(t, cursor);
    }

    /**
     * 关闭数据库的方法
     */
    public void close(){
        sqLiteDatabase.close();
    }


    /************************************接下来是核心代码*********************************/

    /**
     * 利用反射将bean转化为ContentValues
     * @param t
     * @return  若为null则为转化错误
     */
    public static <T> ContentValues bean2ContentValues(T t){
        ContentValues contentValues=new ContentValues();

        Class clazz=t.getClass();

        Field[] fields=clazz.getDeclaredFields();

        try{
            for(Field field:fields){
                field.setAccessible(true);
                Type type=field.getType();

                if(type.equals(boolean.class) || type.equals(Boolean.class)){
                    contentValues.put(field.getName(),(Boolean)field.get(t));

                }else if(type.equals(byte.class) || type.equals(Byte.class)) {
                    contentValues.put(field.getName(),(Byte) field.get(t));

                }else if(type.equals("B[")) {
                    contentValues.put(field.getName(),(byte[]) field.get(t));

                }else if(type.equals(short.class) || type.equals(Short.class)){
                    contentValues.put(field.getName(),(Short)field.get(t));

                }else if(type.equals(int.class) || type.equals(Integer.class.toString())){
                    contentValues.put(field.getName(),(Integer) field.get(t));

                }else if(type.equals(long.class) || type.equals(Long.class)){
                    contentValues.put(field.getName(),(Long)field.get(t));

                }else if(type.equals(float.class) || type.equals(Float.class)){
                    contentValues.put(field.getName(),(Float)field.get(t));

                }else if(type.equals(double.class) || type.equals(Double.class)){
                    contentValues.put(field.getName(),(Double)field.get(t));

                }else if(type.equals(String.class)){
                    contentValues.put(field.getName(),(String)field.get(t));
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();

            /**
             * 转化错误，返回null
             */
            return null;
        }

        return contentValues;
    }

    /**
     * 将cursor转化为ArrayList<T>
     * @param t
     * @param cursor
     * @return
     */
    public static <T> ArrayList<T> cursor2Beans(T t,Cursor cursor){
        ArrayList<T> arrayList=new ArrayList<T>();

        Class clazz=t.getClass();

        Field[] fields=clazz.getDeclaredFields();

        while (cursor.moveToNext()){
            try{
                T tmpClazz=(T)clazz.newInstance();

                for(Field field:fields){
                    field.setAccessible(true);

                    Type type=field.getType();

                    if(type.equals(boolean.class) || type.equals(Boolean.class)){
                        field.set(tmpClazz, Boolean.valueOf(cursor.getString(cursor.getColumnIndex(field.getName()))));

                    }else if(type.equals(byte.class) || type.equals(Byte.class)) {
                        field.set(tmpClazz, Byte.valueOf(cursor.getString(cursor.getColumnIndex(field.getName()))));

                    }else if(type.toString().equals("B[")) {
                        field.set(tmpClazz,(cursor.getString(cursor.getColumnIndex(field.getName()))).getBytes());

                    }else if(type.equals(short.class) || type.equals(Short.class)){
                        field.set(tmpClazz, Short.valueOf(cursor.getString(cursor.getColumnIndex(field.getName()))));

                    }else if(type.equals(int.class) || type.equals(Integer.class)){
                        field.set(tmpClazz, Integer.valueOf(cursor.getString(cursor.getColumnIndex(field.getName()))));

                    }else if(type.equals(long.class) || type.equals(Long.class)){
                        field.set(tmpClazz, Long.valueOf(cursor.getString(cursor.getColumnIndex(field.getName()))));

                    }else if(type.equals(float.class) || type.equals(Float.class)){
                        field.set(tmpClazz, Float.valueOf(cursor.getString(cursor.getColumnIndex(field.getName()))));

                    }else if(type.equals(double.class) || type.equals(Double.class)){
                        field.set(tmpClazz, Double.valueOf(cursor.getString(cursor.getColumnIndex(field.getName()))));

                    }else if(type.equals(String.class)){
                        field.set(tmpClazz, cursor.getString(cursor.getColumnIndex(field.getName())));

                    }
                }

                arrayList.add(tmpClazz);

            }catch (Exception ex){
                ex.printStackTrace();

                /**
                 * 转化错误，返回null
                 */
                return null;
            }
        }
        return arrayList;
    }
}