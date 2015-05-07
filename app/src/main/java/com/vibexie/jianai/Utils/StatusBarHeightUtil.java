package com.vibexie.jianai.Utils;

import android.content.Context;
import android.view.View;

/**
 * 利用反射机制获取状态栏高度的工具类
 * Created by vibexie on 4/25/15.
 */
public class StatusBarHeightUtil {
    /**
     * 在activity中调用
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context){
        Class<?> c = null;
        Object obj = null;
        java.lang.reflect.Field field = null;
        int x = 0;
        int statusBarHeight = 0;
        try{
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
            return statusBarHeight;
        }catch (Exception e){
            e.printStackTrace();
        }

        return statusBarHeight;
    }

    /**
     * 在自定义View中调用
     * @param view
     * @return
     */
    public static int getStatusBarHeight(View view){
        Class<?> c = null;
        Object obj = null;
        java.lang.reflect.Field field = null;
        int x = 0;
        int statusBarHeight = 0;
        try{
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = view.getResources().getDimensionPixelSize(x);
            return statusBarHeight;
        }catch (Exception e){
            e.printStackTrace();
        }

        return statusBarHeight;
    }
}