package com.vibexie.jianai.LoginRegister;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
import android.widget.TextView;
import android.widget.Toast;

import com.vibexie.jianai.Constants.RegisterAndRegisterCmd;
import com.vibexie.jianai.Constants.ServerConf;
import com.vibexie.jianai.R;
import com.vibexie.jianai.Services.XMPPservice.XMPPConnectionManager;
import com.vibexie.jianai.Services.XMPPservice.XMPPService;
import com.vibexie.jianai.Utils.HttpClientUtil;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
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
    private String XMPPUSERNAME;

    /**
     * XMPP用户密码
     */
    private String XMPPPASSWORD;

    /**
     * 标记XMPPConnectNetworkReceiver只注册一次
     */
    private static boolean isXMPPConnectNetworkReceiverRegistered=false;

    /**
     * 广播接收者，在这里声明是为了注销广播
     */
    XMPPConnectNetworkReceiver xmppConnectNetworkReceiver;

    ChatManager chatManager;

    Chat chat;

    /**
     * 相关控件
     */
    private TextView title;
    private CircleProgressBar circleProgressBar;
    private LinearLayout inputLinearLayout,promptLinearLayout;
    private EditText loverName;
    private Button confirm,cancle;
    private AlertDialog mAlertDialog;

    /**
     * 从控件中获取的loverName
     */
    private String name;

    public AddLoverDialog(Context context,String xmppUsername,String xmppPassword){
        this.context=context;
        this.XMPPUSERNAME=xmppUsername;
        this.XMPPPASSWORD=xmppPassword;

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
        mAlertDialog=alertDialog;

        /**
         *  必须获取alertDialog的Window对象，否这不能closeDialog=(Button)window.findViewById(R.id.closeDialog);
         */
        Window window=alertDialog.getWindow();

        /**
         * 获取相关控件
         */
        title=(TextView)window.findViewById(R.id.progress_title_textview);
        circleProgressBar=(CircleProgressBar)window.findViewById(R.id.circleProgressBar);
        inputLinearLayout=(LinearLayout)window.findViewById(R.id.progress_input);
        promptLinearLayout=(LinearLayout)window.findViewById(R.id.progress_prompt);
        loverName=(EditText)window.findViewById(R.id.progress_lovername);
        confirm=(Button)window.findViewById(R.id.progress_confirm);
        cancle=(Button)window.findViewById(R.id.progress_cancel);

        /**
         * 设置circleProgressBar文本
         */
        circleProgressBar.setText("等候您的输入");


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name=loverName.getText().toString();
                if(name.equals("")){
                    Toast.makeText(context,"请输入用户名",Toast.LENGTH_SHORT).show();
                    return;
                }

                chatManager=XMPPService.chatManager;

                chat=chatManager.createChat(name+"@"+ ServerConf.OPENFIRE_SERVER_HOSTNAME, null);

                /**
                 * 发出添加好友请求
                 */
                try {
                    chat.sendMessage(RegisterAndRegisterCmd.REQUEST_ADDLOVER_ADD);
                } catch (XMPPException e) {
                    e.printStackTrace();

                    /**
                     * 发送失败返回
                     */
                    return;
                }

                inputLinearLayout.setVisibility(View.GONE);
                circleProgressBar.setText("等候对方确认");
                title.setText("添加请求已发出");

                confirm.setClickable(false);
            }
        });

        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    /**
     * dismiss Dialog的方法
     */
    private void dismiss(){
        if(mAlertDialog!=null && mAlertDialog.isShowing()){

            /**
             * 注销广播接收者
             */
            if(xmppConnectNetworkReceiver!=null){
                context.unregisterReceiver(xmppConnectNetworkReceiver);
            }

            /**
             * 解绑service
             */
            context.unbindService(serviceConnection);

            mAlertDialog.dismiss();
        }
    }

    /**
     * 对接收到的消息进行处理并进行分发，也就是处理消息的具体实现
     * @param fromWho
     * @param message
     */
    public void distributeMessage(final String fromWho,String message){
        /**
         * 收到请求
         */
        if(message.equals(RegisterAndRegisterCmd.REQUEST_ADDLOVER_ADD)) {

            /**
             * 隐藏输入控件
             */
            inputLinearLayout.setVisibility(View.GONE);

            title.setText("收到" + fromWho + "的请求");
            circleProgressBar.setText("确认添加爱人");

            confirm.setClickable(true);
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    /**
                     * 构建请求url
                     */
                    String url = ServerConf.SERVER_ADDR + "JianaiServer/AddloverAndgetuserinfoServlet";

                    /**
                     * 构建url请求参数
                     */
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("cmd", RegisterAndRegisterCmd.REQUEST_ADDLOVER_ADD));
                    params.add(new BasicNameValuePair("info", XMPPUSERNAME + "&&" + fromWho));

                    /**
                     * 构建handler,在handler中获取并处理结果
                     */
                    Handler handler = new Handler() {
                        @Override
                        public void handleMessage(final Message msg) {
                            super.handleMessage(msg);
                            String result = (String) msg.obj;

                            if(result.equals(RegisterAndRegisterCmd.RESPONSE_LOVERADD_SUCCESS)){
                                /*添加成功,将成功消息返回给对方*/

                                /**
                                 * 在openfire中添加好友，对方将默认接受
                                 */
                                try {
                                    XMPPConnectionManager.getInstance().getXmppConnection().getRoster().createEntry(name + "@" + ServerConf.OPENFIRE_SERVER_HOSTNAME, name, null);
                                } catch (XMPPException e) {
                                    e.printStackTrace();
                                }

                                chatManager=XMPPService.chatManager;
                                chat=chatManager.createChat(fromWho+"@"+ ServerConf.OPENFIRE_SERVER_HOSTNAME, null);
                                try {
                                    chat.sendMessage(RegisterAndRegisterCmd.RESPONSE_LOVERADD_SUCCESS);
                                } catch (XMPPException e) {
                                    e.printStackTrace();
                                }

                                title.setText("添加另外一半成功");
                                circleProgressBar.setText("3秒后将登录");
                                confirm.setClickable(false);

                                Timer timer=new Timer();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        dismiss();
                                    }
                                },3000);

                            }else {
                                /*添加失败，将失败消息返回给对方*/

                                try {
                                    chat.sendMessage(RegisterAndRegisterCmd.RESPONSE_LOVERADD_FAIL);
                                } catch (XMPPException e) {
                                    e.printStackTrace();
                                }

                                title.setText("添加另外一半失败！");
                                circleProgressBar.setText("3秒后将退出");
                                confirm.setClickable(false);

                                Timer timer=new Timer();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        dismiss();
                                    }
                                },3000);
                            }
                        }
                    };

                    /**
                     * 调用请求方法
                     */
                    HttpClientUtil.doPost(url, params, handler);

                    confirm.setClickable(false);
                }
            });

        }else if(message.equals(RegisterAndRegisterCmd.RESPONSE_LOVERADD_SUCCESS)){
            /*被成功添加*/
            title.setText("添加另外一半成功");
            circleProgressBar.setText("3秒后将登录");
            confirm.setClickable(false);

            Timer timer=new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    dismiss();
                }
            },3000);


        }else if(message.equals(RegisterAndRegisterCmd.RESPONSE_LOVERADD_FAIL)){
            /*未被成功添加*/

            title.setText("添加另外一半失败！");
            circleProgressBar.setText("3秒后将退出");
            confirm.setClickable(false);

            Timer timer=new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    dismiss();
                }
            },3000);
        }
    }



    /**********************************以下代码是于service交互的，最好不要修改********************************/

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
            xmppConnectNetworkReceiver=new XMPPConnectNetworkReceiver();
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
}