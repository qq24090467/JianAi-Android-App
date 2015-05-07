package com.vibexie.jianai.Services.XMPPservice;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.vibexie.jianai.Utils.NetworkStateUtil;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 这个service已经完美解决掉线重连，以及网络异常的问题
 * Created by vibexie on 4/28/15.
 */
public class XMPPService extends Service{

    /**
     *LOG TAG
     */
    private static final String TAG="XMPPService";

    /**
     * 从客户端接收消息的信使
     */
    private Messenger receiveMessenger=new Messenger(new IncomingHandler());

    /**
     * 将消息发送给客户端的信使
     */
    private Messenger sendMessenger=null;

    /**
     * 处理从服务器接收到的消息的handler
     */
    private Handler msgFromServerHandler=null;

    /**
     * 登录的用户名
     */
    private String username;

    /**
     * 登录的密码
     */
    private String password;

    /**
     * 这里设置为静态，是为了其它类可以直接获取该chatManager
     */
    public static ChatManager chatManager;


    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG,"XMPPService被绑定");
        return receiveMessenger.getBinder();
    }

    /**
     * 处理从客户端接收到的消息，这里主要是为了将msg.replyTo赋给全局的replyMessenger
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            /**
             * 解析json获取用户名和密码
             */
            JSONObject jsonObject=(JSONObject)msg.obj;
            try {
                username=jsonObject.get("username").toString();
                password=jsonObject.get("password").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            /**
             * 将sendMessager对应于客户端的receiveMessenger
             */
            sendMessenger=msg.replyTo;

            if(receiveMessenger!=null){
                Log.v(TAG,"Service与Client握手成功");
            }

            handleMsgFromServer();
        }
    }

    /**
     * 处理从服务器接收到的消息,将结构到的信息转发给客户端也就是MainActivity
     */
    private void handleMsgFromServer(){
        /**
         * 启动登录与监听消息线程
         */
        new Thread(new XMPPThread()).start();

        msgFromServerHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                /**
                 * 将msg也就是一个json转发给客户端MainActivity
                 */
                Message message=Message.obtain();
                message.obj=msg.obj;

                try {
                    sendMessenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * 登录openfire和接收消息的线程
     */
    private class XMPPThread implements Runnable{
        @Override
        public void run() {
            if(!NetworkStateUtil.isNetWorkConnected(getApplicationContext())){
                Log.v(TAG, "网络未连接...");
                return;
            }

            Log.v(TAG, "登录"+Thread.currentThread().getName()+"线程启动......");

            XMPPConnectionManager xmppConnectionManager = XMPPConnectionManager.getInstance();

            if(xmppConnectionManager.connect()){
                Log.v(TAG,"XMPP连接成功");
            }else {
                Log.v(TAG,"XMPP连接失败");
                return;
            }

            if(xmppConnectionManager.login(username, password)){
                Log.v(TAG,"用户登录XMPP成功");
            }else {
                Log.v(TAG,"用户登录XMPP失败");
                return;
            }

            XMPPConnection xmppConnection = xmppConnectionManager.getXmppConnection();

            /**
             * 每一次连接服务器，都要刷新chatManager对象
             */
            chatManager=xmppConnection.getChatManager();

            /**
             * 登录状态为在线
             */
            Presence presence = new Presence(Presence.Type.available);
            xmppConnection.sendPacket(presence);

            /**
             * 收到好友邀请后manual表示需要经过同意,accept_all表示不经同意自动为好友
             */
            Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);

            /**
             * 获取聊天管理对象
             */
            final ChatManager chatManager = xmppConnection.getChatManager();

            class MyChatManagerListener implements ChatManagerListener{
                @Override
                public void chatCreated(Chat chat, final boolean b) {

                    chat.addMessageListener(new MessageListener() {
                        @Override
                        public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {

                            Log.v(TAG, "收到来自" + message.getFrom() + "的消息:" + message.getBody());

                            /**
                             * message.getFrom()的格式为test2@vibexie-server，将名字提取出来
                             */
                            String tmp = message.getFrom();
                            String fromWho = tmp.substring(0, tmp.lastIndexOf("@"));

                            /**
                             * 构建json格式为，{"from":"xxx","msg":"xxx"}
                             */
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("from", fromWho);
                                jsonObject.put("msg", message.getBody());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            android.os.Message messageToHandler = Message.obtain();

                            /**
                             * 将json传给handler
                             */
                            messageToHandler.obj = jsonObject;
                            msgFromServerHandler.sendMessage(messageToHandler);
                        }
                    });
                }
            }

            /**
             *添加监听器，接收来自服务器的转发过来的消息，是条独立的线程
             */
            MyChatManagerListener myChatManagerListener=new MyChatManagerListener();
            Log.v(TAG,"开始监听消息......");
            chatManager.addChatListener(myChatManagerListener);


            class MyConnectionListener implements ConnectionListener{

                @Override
                public void connectionClosed() {

                }

                @Override
                public void connectionClosedOnError(Exception e) {

                    Log.v(TAG,"连接发生错误:connectionClosedOnError,10秒后立即重新连接......");

                    /**
                     * 10秒后立即重新连接服务器
                     */
                    Timer timer=new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            if(!NetworkStateUtil.isNetWorkConnected(getApplicationContext())){
                                /**
                                 * 经过跟踪测试，一旦新线程动，本线程立即结束
                                 */
                                new Thread(new XMPPThread()).start();

                            }
                        }
                    },10000);
                }

                @Override
                public void reconnectingIn(int i) {

                }

                @Override
                public void reconnectionSuccessful() {

                }

                @Override
                public void reconnectionFailed(Exception e) {

                }
            }

            MyConnectionListener myConnectionListener=new MyConnectionListener();
            Log.v(TAG,"开始监听连接状态......");
            xmppConnection.addConnectionListener(myConnectionListener);
        }
    }
}