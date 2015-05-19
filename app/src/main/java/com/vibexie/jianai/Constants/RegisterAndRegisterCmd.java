package com.vibexie.jianai.Constants;

/**
 * 用户注册servlet相关的指令，与客户端相统一,因为请求参数都是字符串类型，所以直接将cmd设置为String类型
 * Title  : RegisterAndRegisterCmd.java
 * Company: ZhenBot
 * Author : Vibe Xie  @www.vibexie.com
 * Time   : Apr 19, 2015 8:47:14 PM
 * Copyright: Copyright (c) 2015
 * Description:
 */
public class RegisterAndRegisterCmd {
    /**
     * 请求验证码，携带的参数是username和要验证的邮箱email
     */
    public static String REQUEST_VERIFING_CODE="100";

    /**
     * 请求匹配验证码是否与服务端验证码相同
     */
    public static String REQUEST_MATCH_VERIFYING_CODE="110";

    /**
     * 请求完成注册
     */
    public static String REQUEST_COMPETE_REGISTER="120";

    /**
     * 请求登录
     */
    public static String REQUEST_LOGIN="130";

    /**
     * 请求添加lover
     */
    public static final String REQUEST_ADDLOVER_ADD="140";

    /**
     * 请求获取自己信息
     */
    public static final String REQUEST_MY_INFO="150";

    /**
     * 请求获取lover信息
     */
    public static final String REQUEST_LOVER_INFO="160";



    /**
     * 服务器返回，成功发送验证码给该邮箱
     */
    public static String RESPONSE_EMAIL_SEND_SUCCESS="200";

    /**
     * 服务器返回，发送验证码给该邮箱失败
     */
    public static String RESPONSE_EMAIL_SEND_FAIL="201";

    /**
     * 服务器返回，请求的用户名已经注册
     */
    public static String RESPONSE_USERNAME_EXIST="203";

    /**
     * 服务器返回，请求的邮箱已经注册
     */
    public static String RESPONSE_EMAIL_EXIST="204";

    /**
     * 服务器返回，请求的验证码与数据库中的验证码一致，匹配成功
     */
    public static String RESPONSE_MATCH_VERIFYING_CODE_OK="210";

    /**
     * 服务器返回，请求的验证码与数据库中的验证码不一致
     */
    public static String RESPONSE_MATCH_VERIFYING_CODE_FAIL="211";

    /**
     * 服务器返回，注册成功
     */
    public static String RESPONSE_COMPETE_REGISTER_OK="220";

    /**
     * 服务器返回，验证码已经过期
     */
    public static String RESPONSE_VERIFYING_CODE_OVERDUE="221";

    /**
     * 服务器返回，注册失败，服务器故障
     */
    public static String RESPONSE_COMPETE_REGISTER_FAIL="222";

    /**
     * 服务器返回，登录成功
     */
    public static String RESPONSE_LOGIN_SUCCESS="230";

    /**
     * 服务器返回，密码错误
     */
    public static String RESPONSE_LOGIN_PASSWORD_WRONG="231";

    /**
     * 服务器返回，用户不存在
     */
    public static String RESPONSE_LOGIN_USER_NOT_EXIST="232";

    /**
     * 服务器返回，添加另一半成功
     */
    public static String RESPONSE_LOVERADD_SUCCESS="240";

    /**
     * 服务器返回，添加另一半失败
     */
    public static String RESPONSE_LOVERADD_FAIL="241";
}