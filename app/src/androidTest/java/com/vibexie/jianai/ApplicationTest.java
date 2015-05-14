package com.vibexie.jianai;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.vibexie.jianai.Utils.FreshDatasManager;
import com.vibexie.jianai.Dao.Bean.ChatMsgBean;
import com.vibexie.jianai.Dao.DBHelper.UserDBHelper;
import com.vibexie.jianai.Dao.DBManager.DBManager;
import com.vibexie.jianai.Utils.TimeUtil;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    private final String TAG = "appTest";

    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @SmallTest
    public void test() {
        /**
         * 构建bean
         */
        ChatMsgBean chatMsgBean = new ChatMsgBean();
        chatMsgBean.setMsgTime(TimeUtil.getCurrentStandardTime());
        chatMsgBean.setMsgContent("你会吗");
        chatMsgBean.setFromOrTo(1);
        chatMsgBean.setRead(0);



        //dbManager.insertBean("friend_msg", chatMsgBean);

//        Cursor cursor = dbManager.getSqLiteDatabase().rawQuery("select count(*) from friend_msg", null);
//        cursor.moveToFirst();
//        int num = cursor.getInt(0);
//
//        Log.v(TAG,"----------------"+num);
//
//        Cursor cursor1 = dbManager.getSqLiteDatabase().rawQuery("select * from friend_msg limit 487," + 20 + ";", null);
//        while (cursor1.moveToNext()) {
//            Log.v(TAG, cursor1.getString(0));
//        }

//        UserDBHelper userDBHelper = new UserDBHelper(getContext(), "100002.db");
//        DBManager dbManager = new DBManager(userDBHelper);
//
//        FreshDatasManager<ChatMsgBean> freshDatasManager=new FreshDatasManager<ChatMsgBean>(dbManager.getSqLiteDatabase(),new ChatMsgBean(),"friend_msg",10);
//
//
//
//        for(int i=1;;i++){
//            ArrayList<ChatMsgBean> list=freshDatasManager.getRefreshBeans();
//
//            if(list==null){
//                return;
//            }
//
//            Log.v(TAG,"第"+i+"组数据");
//            for (ChatMsgBean bean:list){
//                Log.v(TAG,String.valueOf(bean.get_Id()));
//            }
//        }
        LinkedList<String> linkedList=new LinkedList<String>();
        linkedList.add("1");
        linkedList.add("2");
        linkedList.add("3");
        linkedList.add("4");
        linkedList.add("5");
        linkedList.add("6");

        ArrayList<String> arrayList=new ArrayList<String>();
        arrayList.add("111");
        arrayList.add("222");
        linkedList.addAll(0,arrayList);
        Log.v(TAG,linkedList.toString());
    }
}