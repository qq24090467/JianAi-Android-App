package com.vibexie.jianai.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.vibexie.jianai.R;
import com.vibexie.jianai.Utils.NetworkStateUtil;

/**
 * 设置网络的AlertDialog
 * 这是一个非完全意义上的自定义View，仅仅类似自定义View，在项目中暂且称为自定义View
 * Created by vibexie on 4/23/15.
 *
 * 使用说明：1.androidMainfest,加入需要的权限
 *              <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
 *              <uses-permission android:name="android.permission.INTERNET"></uses-permission>
 *              <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE"></uses-permission>
 *              <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
 *              <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"></uses-permission>
 *         2.项目引入NetworkStateUtil类
 *         3.项目加入xml布局需要3张的图片
 *         4.项目加入network_settings_alertdialog_layout.xml布局文件
 *         5.使用：NetworkSettingsAlertDialog networkSettingsAlertDialog=new NetworkSettingsAlertDialog(MainActivity.this);
 *                networkSettingsAlertDialog.show();
 *
 */
public class NetworkSettingsAlertDialog {
    private Context context;
    private Button closeDialog;
    private Switch wifiSwitch;
    private Switch mobileSwitch;

    /**
     * 构造函数
     * @param context   上下文对象
     */
    public NetworkSettingsAlertDialog(Context context){
        this.context=context;
    }

    /**
     * 显示NetworkSettingsAlertDialog的方法
     */
    public void show(){
        LayoutInflater layoutInflater=LayoutInflater.from(context);
        View view=layoutInflater.inflate(R.layout.network_settings_alertdialog_layout,null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        /**
         * 设置布局
         */
        builder.setView(view);

        builder.setCancelable(false);

        builder.create();

        /**
         * 上转型为AlertDialog，因为AlertDialog有dismiss()方法
         */
        final AlertDialog alertDialog=builder.show();

        /**
         *  必须获取alertDialog的Window对象，否这不能closeDialog=(Button)window.findViewById(R.id.closeDialog);
         */
        Window window=alertDialog.getWindow();

        closeDialog=(Button)window.findViewById(R.id.closeDialog);
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        wifiSwitch=(Switch)window.findViewById(R.id.wifiSwitch);
        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    NetworkStateUtil.setWifiEnabled(context, true);
                }else {
                    NetworkStateUtil.setWifiEnabled(context,false);
                }
            }
        });

        mobileSwitch=(Switch)window.findViewById(R.id.mobileSwitch);
        mobileSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    NetworkStateUtil.setMobileDataEnabled(context,true);
                }else {
                    NetworkStateUtil.setMobileDataEnabled(context,false);
                }
            }
        });
    }
}