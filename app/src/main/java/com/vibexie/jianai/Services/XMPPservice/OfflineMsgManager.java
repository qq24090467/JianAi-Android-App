package com.vibexie.jianai.Services.XMPPservice;

import android.os.Handler;
import android.util.Log;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.OfflineMessageManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by vibexie on 5/18/15.
 */
public class OfflineMsgManager {
    private static final String TAG="OfflineMsg";
    private XMPPConnection xmppConnection;
    private Handler handler;

    OfflineMsgManager(XMPPConnection xmppConnection,Handler handler){
        this.handler=handler;
        this.xmppConnection=xmppConnection;
    }
    public void getOffLineMsg(){
        OfflineMessageManager messageManager=new OfflineMessageManager(xmppConnection);

        try {
            Log.v(TAG, "收到"+messageManager.getMessageCount()+"条离线消息");

            Iterator<Message> iterator=messageManager.getMessages();

            while (iterator.hasNext()){
                Message message=iterator.next();

                Log.v(TAG, "收到来自" + message.getFrom() + "的离线消息:" + message.getBody());

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

                android.os.Message messageToHandler = android.os.Message.obtain();

                /**
                 * 将json传给handler
                 */
                messageToHandler.obj = jsonObject;
                handler.sendMessage(messageToHandler);
            }

            /**
             * 最后删除所有的离线消息
             */
            messageManager.deleteMessages();

            /**
             * 登录状态为在线
             */
            Presence presence = new Presence(Presence.Type.available);
            xmppConnection.sendPacket(presence);

        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }
}
