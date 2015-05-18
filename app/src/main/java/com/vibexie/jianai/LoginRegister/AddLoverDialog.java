package com.vibexie.jianai.LoginRegister;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.vibexie.jianai.Dao.Bean.ChatMsgBean;
import com.vibexie.jianai.Dao.DBHelper.UserDBHelper;
import com.vibexie.jianai.Dao.DBManager.DBManager;
import com.vibexie.jianai.R;
import com.vibexie.jianai.Services.XMPPservice.XMPPService;
import com.vibexie.jianai.Utils.TimeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by vibexie on 5/18/15.
 */
public class AddLoverDialog {
    Context context;

    /**
     * XMPP用户名
     */
    private String XMPPUSERNAME="test2";

    /**
     * XMPP用户密码
     */
    private String XMPPPASSWORD="test2";

    /**
     * 标记XMPPConnectNetworkReceiver只注册一次
     */
    private static boolean isXMPPConnectNetworkReceiverRegistered=false;

    /**
     * 相关控件
     */
    private CircleProgressBar circleProgressBar;
    private LinearLayout inputLinearLayout,promptLinearLayout;
    private EditText loverName;
    private Button confirm,cancle;

    public AddLoverDialog(Context context){
        this.context=context;

        /**
         * 初始化XMPP服务
         */
        initService(XMPPUSERNAME,XMPPPASSWORD);
    }

    public void show(){
        LayoutInflater layoutInflater=LayoutInflater.from(context);
        View view=layoutInflater.inflate(R.layout.activity_login_addlover_layout, null);

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

        /**
         * 获取相关控件
         */
        circleProgressBar=(CircleProgressBar)window.findViewById(R.id.circleProgressBar);
        inputLinearLayout=(LinearLayout)window.findViewById(R.id.progress_input);
        promptLinearLayout=(LinearLayout)window.findViewById(R.id.progress_prompt);
        loverName=(EditText)window.findViewById(R.id.progress_lovername);
        confirm=(Button)window.findViewById(R.id.progress_confirm);
        cancle=(Button)window.findViewById(R.id.progress_cancel);

        /**
         * 设置circleProgressBar文本
         */
        circleProgressBar.setText("等候输入");


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loverName.getText().toString().equals("")){
                    Toast.makeText(context,"请输入用户名",Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });

        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * 退出解绑service
                 */
                context.unbindService(serviceConnection);

                alertDialog.dismiss();
            }
        });
    }








    /**
     * 接收消息的信使，在这里调用distributeMessage对消息进行处理
     */
    private Messenger receiveMessenger=new Messenger(new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            JSONObject jsonObject=(JSONObject)msg.obj;

            /**
             * 解析json,并调用distributeMessage分发消息
             */
            try {
                String fromWho=jsonObject.getString("from");
                String message=jsonObject.getString("msg");

                distributeMessage(fromWho,message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    });

    /**
     * 发送消息的信使
     */
    private Messenger sendMessenger=null;

    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sendMessenger=new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * 网络已经重新连接的广播接收者
     */
    class XMPPConnectNetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            /*如果网络已连接,与service握手，请求连接XMPP*/

            initService(XMPPUSERNAME,XMPPPASSWORD);

            Toast.makeText(context, "网络已连接,正在重新登录...", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 初始化service
     * @param username  用户名
     * @param password  密码
     */
    private void initService(final String username,final String password){

        if(!isXMPPConnectNetworkReceiverRegistered){
            /**
             * 用代码注册广播接收者，当NetWorkReceivr得知网络已经连接，立即通知登录XMPP
             */
            XMPPConnectNetworkReceiver xmppConnectNetworkReceiver=new XMPPConnectNetworkReceiver();
            IntentFilter intentFilter=new IntentFilter();
            intentFilter.addAction("com.vibexie.jianai.MainActivityModule.MainActivity.XMPP");
            context.registerReceiver(xmppConnectNetworkReceiver, intentFilter);
        }

        isXMPPConnectNetworkReceiverRegistered=true;

        /**
         * 绑定XMPPService
         */
        context.bindService(new Intent(context, XMPPService.class), serviceConnection, Context.BIND_AUTO_CREATE);

        /**
         * 因为绑定service需要一小段时间，必须在成功绑定service后才能与service握手，所以延迟1000ms
         */
        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message=Message.obtain();

                /**
                 * 构建json {"username":"xxx","password":"xxx"}
                 */
                JSONObject jsonObject=new JSONObject();
                try {
                    jsonObject.put("username",username);
                    jsonObject.put("password",password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                message.obj=jsonObject;

                message.replyTo=receiveMessenger;

                try {
                    sendMessenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        },1000);
    }

    /**
     * 对接收到的消息进行处理并进行分发，也就是处理消息的具体实现
     * @param fromWho
     * @param message
     */
    public void distributeMessage(String fromWho,String message){


    }
}