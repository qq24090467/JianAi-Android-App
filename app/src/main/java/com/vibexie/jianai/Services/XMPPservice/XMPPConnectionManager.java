package com.vibexie.jianai.Services.XMPPservice;

import com.vibexie.jianai.Constants.ServerConf;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

/**
 * 单例模式，此类的使用步骤:1.connect()建立连接
 *                      2.getXmppConnection()需要连接对象的话可以获取连接对象
 *                      3.login(String username,String password)登录
 *                      4.disconnect()断开本连接
 *
 * Created by vibexie on 4/29/15.
 */
public class XMPPConnectionManager {

    private static XMPPConnectionManager xmppConnectionManager;

    private XMPPConnection xmppConnection;

    private ConnectionConfiguration connectionConfiguration;

    private XMPPConnectionManager(){

    }

    public static XMPPConnectionManager getInstance(){
        if(xmppConnectionManager==null){
            xmppConnectionManager=new XMPPConnectionManager();
        }

        return xmppConnectionManager;
    }

    private ConnectionConfiguration getConnectionConfiguration() {
        if (connectionConfiguration==null) {
            /**
             * 连接配置
             */
            connectionConfiguration = new ConnectionConfiguration(ServerConf.OPENFIRE_SERVER_IP, 5222, ServerConf.OPENFIRE_SERVER_HOSTNAME);
            connectionConfiguration.setReconnectionAllowed(true);
            connectionConfiguration.setSendPresence(true);
        }

        return connectionConfiguration;
    }

    /**
     * 建立连接
     * @return true/false
     */
    public boolean connect(){
        /**
         * 线将xmppConnection置空，重新建立连接
         */
        xmppConnection=null;

        try {
            getXmppConnection().connect();
        } catch (XMPPException e) {
            e.printStackTrace();

            /**
             * 连接失败，返回null
             */
            return false;
        }

        return true;
    }

    /**
     * 获取XMPPConnection对象,外部必须在调用connect()后才能调用此方法
     * @return
     */
    public XMPPConnection getXmppConnection() {

        if(xmppConnection==null) {

            /**
             * 建立XMPP连接
             */
            xmppConnection = new XMPPConnection(getConnectionConfiguration());

        }

        return xmppConnection;
    }

    /**
     * 登录
     * @param username
     * @param password
     * @return  true/false
     */
    public boolean login(String username,String password){

        try {
            getXmppConnection().login(username,password);
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 断开连接
     */
    public void disconnect(){
        getXmppConnection().disconnect();
    }
}