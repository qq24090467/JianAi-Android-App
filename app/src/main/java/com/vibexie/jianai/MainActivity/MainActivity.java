package com.vibexie.jianai.MainActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.vibexie.jianai.Chat.ChatActivity;
import com.vibexie.jianai.Dao.Bean.ChatMsgBean;
import com.vibexie.jianai.Dao.DBHelper.UserDBHelper;
import com.vibexie.jianai.Dao.DBManager.DBManager;
import com.vibexie.jianai.Services.XMPPservice.XMPPService;
import com.vibexie.jianai.Utils.ActivityStackManager;

import com.vibexie.jianai.R;
import com.vibexie.jianai.Utils.RoundImageView;
import com.vibexie.jianai.Utils.TimeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity{
    /**
     * 持有SlidingView.InnerClassOfSlidingView对象，用于button控制HorizontalScrollView的滑动
     */
    private SlidingView.InnerClassOfSlidingView innerClassOfSlidingView=new SlidingView.InnerClassOfSlidingView();

    private ImageButton chatImageButton;

    /*以下3个参数在sqlite中获取*/

    /**
     * XMPP用户名
     */
    private static final String XMPPUSERNAME="test1";

    /**
     * XMPP用户密码
     */
    private static final String XMPPPASSWORD="test1";

    /**
     * XMPP lover的用户名
     */
    private static final String XMPPLOVERNAME="test2";

    /**
     * 标记XMPPConnectNetworkReceiver只注册一次
     */
    private static boolean isXMPPConnectNetworkReceiverRegistered=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * 压入到ActivityStack中
         */
        ActivityStackManager.getInstance().push(this);

        /**
         * 初始化XMPP服务
         */
        initService(XMPPUSERNAME,XMPPPASSWORD);

        chatImageButton=(ImageButton)this.findViewById(R.id.chat);


        chatImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });

        RoundImageView myHead=(RoundImageView)this.findViewById(R.id.sliding_left_my_head);

        //myHead.setImageResource(R.drawable.main_activity_head);
        //myHead.invalidate();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        /**
         * 从ActivityStack中弹出
         */
        ActivityStackManager.getInstance().pop(this);

        /**
         * 解除绑定XMPPService
         */
        unbindService(serviceConnection);


    }

    /*************************************************************************************************/

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

            innerClassOfSlidingView.toggleOfSlidingRemind();
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
    class XMPPConnectNetworkReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            /*如果网络已连接,与service握手，请求连接XMPP*/

            initService(XMPPUSERNAME,XMPPPASSWORD);

            Toast.makeText(context,"网络已连接,正在重新登录...",Toast.LENGTH_SHORT).show();
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
            MainActivity.this.registerReceiver(xmppConnectNetworkReceiver,intentFilter);
        }

        isXMPPConnectNetworkReceiverRegistered=true;

        /**
         * 绑定XMPPService
         */
        bindService(new Intent(MainActivity.this,XMPPService.class),serviceConnection, Context.BIND_AUTO_CREATE);

        /**
         * 因为绑定service需要一小段时间，必须在成功绑定service后才能与service握手，所以延迟1500ms
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
        },1500);
    }

    /**
     * 对接收到的消息进行处理并进行分发，也就是处理消息的具体实现
     * @param fromWho
     * @param message
     */
    public void distributeMessage(String fromWho,String message){

        /**
         * 收到lover的消息
         */
        if(fromWho.equals(XMPPLOVERNAME)){
            ChatMsgBean chatMsgBean=new ChatMsgBean();
            chatMsgBean.setMsgTime(TimeUtil.getCurrentStandardTime());
            chatMsgBean.setMsgContent(message);
            chatMsgBean.setFromOrTo(0);
            chatMsgBean.setRead(0);

            /**
             * 插入该消息在数据表中的_id
             */
            int _id=0;

            UserDBHelper userDBHelper=new UserDBHelper(MainActivity.this,"100002.db");
            DBManager dbManager=new DBManager(userDBHelper);

            if(dbManager.insertBean("friend_msg",chatMsgBean)){

                String sql="select max(_id) from friend_msg;";
                Cursor cursor=dbManager.getSqLiteDatabase().rawQuery(sql,null);
                while (cursor.moveToNext()){
                    _id=cursor.getInt(0);
                }
            }

            /**
             * 广播通知ChatActivity从数据库中读取该消息并显示,广播中携带了该消息的_id
             */
            Intent intent=new Intent();
            intent.setAction("com.vibexie.jianai.ChatModule.ChatActivity.MsgUpdate");
            intent.putExtra("_id",_id);
            getApplicationContext().sendBroadcast(intent);

            /**
             * 关闭数据库管理连接
             */
            dbManager.close();
        }
    }
}