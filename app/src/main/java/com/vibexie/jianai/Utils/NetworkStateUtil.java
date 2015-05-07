package com.vibexie.jianai.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

import java.lang.reflect.Method;

/**
 * 网络连接状态的工具类，并提供设置网络的方法
 * Created by vibexie on 4/22/15.
 *
 * 使用时请参考sdk doc:/docs/training/basics/network-ops/managing.html
 *
 * 注意必须添加这3个权限
 * <uses-permission android:name="android.permission.INTERNET" />
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 * <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE"></uses-permission>
 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
 * <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"></uses-permission>
 */
public class NetworkStateUtil {

    /**
     * 判断网络是否连接
     * @param context
     * @return
     */
    public static boolean isNetWorkConnected(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }



    /**
     * 判断wifi是否连接
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return networkInfo.isConnected();
    }



    /**
     * 判断移动网络是否连接
     * @param context
     * @return
     */
    public static boolean isMobileConnected(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        return networkInfo.isConnected();
    }



    /**
     * 调用系统设置进行设置网络
     * @param context
     */
    public static void goToSetNetWork(Context context){
        Intent intent=null;

        /**
         * 判断安卓版本
         */
        if(Build.VERSION.SDK_INT>10) {
            /*3.0以上版本*/

            intent=new Intent(Settings.ACTION_WIFI_SETTINGS);
        }else {
            /*3.0以下版本*/

            intent=new Intent();
            intent.setClassName("com.android.settings","com.android.settings.WifiSettings");
        }

        context.startActivity(intent);
    }



    /**
     * 设置wifi状态
     * 注意需要添加权限：<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
     *                <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"></uses-permission>
     * @param context
     * @param enabled
     */
    public static void setWifiEnabled(Context context,boolean enabled){
        WifiManager wifiManager=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(enabled);
    }


    /**
     * 设置移动网络状态
     * 注意需要添加权限：<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE"></uses-permission>
     * @param context   上下文
     * @param enabled   是否开启移动网络连接
     */
    public static void setMobileDataEnabled(Context context, boolean enabled) {
        /**
         * 必须采用反射机制获取系统隐藏的功能
         */

//        /**
//          *StackOverStack解决方案：
//          */
//        try {
//            ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            Class conmanClass = Class.forName(conman.getClass().getName());
//            Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
//            iConnectivityManagerField.setAccessible(true);
//            Object iConnectivityManager = iConnectivityManagerField.get(conman);
//            Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
//            Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
//            setMobileDataEnabledMethod.setAccessible(true);
//
//            setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
//
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

        /**
         * 进行简化
         */
        ConnectivityManager connectivityManager = null;
        Class connectivityManagerClz = null;
        try {
            connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
            connectivityManagerClz = connectivityManager.getClass();
            Method method = connectivityManagerClz.getMethod("setMobileDataEnabled", new Class[] { boolean.class });
            method.invoke(connectivityManager, enabled);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}