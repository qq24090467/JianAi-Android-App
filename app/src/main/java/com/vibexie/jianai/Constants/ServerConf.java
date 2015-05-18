package com.vibexie.jianai.Constants;

/**
 * 服务器的相关配置常量
 * Created by vibexie on 4/21/15.
 */
public class ServerConf {

    /**
     * 服务器对用户密码加密的密钥，这个值在第一次发布APP的时候一旦设定，后续版本必须不能改变这个值
     */
    public  static String PASSWORD_KEY="NH3UOaCL6wN1JOo";

    /**
     * openfire服务器IP地址，使用openfire默认端口5222
     */
    public static String OPENFIRE_SERVER_IP="192.168.31.100";

    /**
     * openfire服务器主机名
     */
    public static String OPENFIRE_SERVER_HOSTNAME="vibexie-workstation";

    /**
     * Servlet服务器地址,必须带端口，如：http://vibexie.com:8080/,默认80端口
     */
    public static String SERVER_ADDR="http://192.168.31.100:8080/";

    /**
     * 用户注册的servlet URL地址
     */
    public static String REGISTER_SERVLET_URL=SERVER_ADDR+"JianaiServer/RegisterServlet";

    /**
     * 用户登录的servlet URL地址
     */
    public static String LOGIN_SERVLET_URL=SERVER_ADDR+"JianaiServer/LoginServlet";
}