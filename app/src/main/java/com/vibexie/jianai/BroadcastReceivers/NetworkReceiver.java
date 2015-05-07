package com.vibexie.jianai.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

import com.vibexie.jianai.Utils.NetworkStateUtil;

/**
 * 监听网络是否断开，由于无法在BroadcastReceiver中更新UI，只能使用Toast通知用户
 * Created by vibexie on 4/23/15.
 */
public class NetworkReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            if(!NetworkStateUtil.isNetWorkConnected(context)){
                /*监听到网络断开*/

                Toast.makeText(context,"网络已断开,请检查您的网络",Toast.LENGTH_LONG).show();

            }else {
                /*监听到网络连接成功*/

                /**
                 * 通知MainActivity登录XMMP
                 */
                Intent intent1=new Intent();
                intent1.setAction("com.vibexie.jianai.MainActivityModule.MainActivity.XMPP");
                context.sendBroadcast(intent1);
            }
        }
    }
}
