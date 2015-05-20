package com.vibexie.jianai.LoginRegister;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.vibexie.jianai.Dao.Bean.UserBean;
import com.vibexie.jianai.Dao.DBHelper.UserDBHelper;
import com.vibexie.jianai.Dao.DBManager.DBManager;
import com.vibexie.jianai.Utils.ActivityStackManager;
import com.vibexie.jianai.Constants.RegisterAndRegisterCmd;
import com.vibexie.jianai.Constants.ServerConf;
import com.vibexie.jianai.MainActivity.MainActivity;
import com.vibexie.jianai.R;
import com.vibexie.jianai.Utils.NetworkSettingsAlertDialog;
import com.vibexie.jianai.Utils.BlowfishUtils.BlowfishUtil;
import com.vibexie.jianai.Utils.HttpClientUtil;
import com.vibexie.jianai.Utils.NetworkStateUtil;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *  Created by vibexie on 3/7/15
 *  LoginActivity为程序的登录界面，包含输入用户名，密码，还有登录和注册按钮
 *  另外，保存密码和自动登录信息保存在sharePerfermance中
 */
public class LoginActivity extends Activity {

    /**
     * 声明所有的控件
     */
    private ProgressDialog progressDialog;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerBubtton;
    private CheckBox remenberCheckBox;
    private CheckBox autologinCheckBox;

    /**
     * 声明从EditText控件中得到的数据
     */
    private String username;
    private String password;

    private final String LOGIN_PREFERENCES_NAME="login_preferences";


    /**
     * 对用户保存的密码和自动登录选项进行处理
     */
    SharedPreferences loginPreferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /**
         * 压入到ActivityStack中
         */
        ActivityStackManager.getInstance().push(this);

        initWidgets();

        initClickListener();

        initSharedPreferences();

        if(loginPreferences.contains("username")){
            usernameEditText.setText(loginPreferences.getString("username",null));

            /**
             * 对密码进行解密
             */
            passwordEditText.setText(new BlowfishUtil(ServerConf.PASSWORD_KEY).decryptString(loginPreferences.getString("password", null)));
            remenberCheckBox.setChecked(true);

            if(loginPreferences.contains("antologin") && loginPreferences.getBoolean("antologin",false)==true){

                autologinCheckBox.setChecked(true);

                if(NetworkStateUtil.isNetWorkConnected(LoginActivity.this)){
                    /*网络连接上的话，向服务器请求新的数据，因为可能数据发生了改变*/

                    /**
                     * 自动调用登录操作
                     */
                    loginButton.callOnClick();

                }else {
                    /*无法联网的情况下，携带保存的用户名，密码，用户id跳转到MainActivity*/

                    /**
                     * 切换到MainActivity，并finish本activity，这里同时还要携带自己的用户名和密码，用户id及lover用户名过去
                     */
                    Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                    intent.putExtra("username",usernameEditText.getText().toString());
                    intent.putExtra("password",passwordEditText.getText().toString());
                    intent.putExtra("userid",loginPreferences.getInt("userid",0));
                    startActivity(intent);
                    finish();
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        /**
         * 从ActivityStack中弹出
         */
        ActivityStackManager.getInstance().pop(this);

    }

    private void initWidgets(){
        usernameEditText = (EditText) this.findViewById(R.id.username);
        passwordEditText = (EditText) this.findViewById(R.id.password);
        loginButton = (Button) this.findViewById(R.id.login);
        registerBubtton = (Button) this.findViewById(R.id.register);
        remenberCheckBox = (CheckBox) this.findViewById(R.id.remember);
        autologinCheckBox = (CheckBox) this.findViewById(R.id.autologin);

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("温馨提示");
        progressDialog.setMessage("正在登录...");
        progressDialog.setCancelable(false);

    }

    /**
     * 初始化button的监听事件
     */
    private void initClickListener(){
        /**
         * 登录按钮监听事件
         */
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isAllEditTextEdited()) {
                    /*用户名和密码未输入，直接返回*/

                    return;
                }

                if (!NetworkStateUtil.isNetWorkConnected(getApplicationContext())) {
                    /*网络未连接,提示用户设置网络*/

                    NetworkSettingsAlertDialog networkSettingsAlertDialog = new NetworkSettingsAlertDialog(LoginActivity.this);
                    networkSettingsAlertDialog.show();

                    /*返回，不进行请求*/
                    return;
                }

                /**
                 * 构建请求url
                 */
                String url = ServerConf.LOGIN_SERVLET_URL;

                /**
                 * 构建url请求参数
                 */
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("cmd", RegisterAndRegisterCmd.REQUEST_LOGIN));
                params.add(new BasicNameValuePair("username", username));
                /**
                 * 对密码进行加密
                 */
                params.add(new BasicNameValuePair("password", new BlowfishUtil(ServerConf.PASSWORD_KEY).encryptString(password)));

                /*处理请求结果*/
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        /**
                         * 请求结果
                         */
                        String result = msg.obj.toString();

                        if (result.equals("refused")) {
                                    /*请求超时或服务器关闭*/
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "请求超时或服务器关闭", Toast.LENGTH_SHORT).show();

                        } else if (result.equals(RegisterAndRegisterCmd.RESPONSE_LOGIN_SUCCESS)) {
                            /*登录成功*/

                            progressDialog.dismiss();

                            workWhenLoginSucceed();

                        } else if (result.equals(RegisterAndRegisterCmd.RESPONSE_LOGIN_PASSWORD_WRONG)) {
                            /*密码错误*/

                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "密码错误", Toast.LENGTH_SHORT).show();

                        } else if (result.equals(RegisterAndRegisterCmd.RESPONSE_LOGIN_USER_NOT_EXIST)) {
                            /*用户不存在*/

                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "用户不存在", Toast.LENGTH_SHORT).show();
                        }
                    }
                };

                /**
                 * 进行请求
                 */
                progressDialog.show();
                HttpClientUtil.doPost(url, params, handler);
            }
        });

        /**
         * 注册按钮监听事件，跳转到注册界面
         */
        registerBubtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterForVerifyingCodeActivity.class);
                startActivity(registerIntent);
            }
        });
    }


    /**
     * 初始化
     */
    private void initSharedPreferences(){
        loginPreferences=getSharedPreferences(LOGIN_PREFERENCES_NAME, Context.MODE_PRIVATE);
        editor=loginPreferences.edit();

        /**
         * 第一次启动App,创建Preferences文件
         */
        File file=new File("/data/data/"+getPackageName().toString()+"/shared_prefs",LOGIN_PREFERENCES_NAME+".xml");
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 判断用户名和密码是否已经输入
     * @return
     */
    private boolean isAllEditTextEdited(){
        username=usernameEditText.getText().toString();
        password=passwordEditText.getText().toString();

        if(username.equals("")){
            Toast.makeText(getApplicationContext(),"请输入用户名",Toast.LENGTH_SHORT).show();
            return false;
        }

        if(password.equals("")){
            Toast.makeText(getApplicationContext(),"请输入密码",Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    /**
     * 成功登录后的一些处理，如添加lover,获取自己和lover信息，跳转到MainActivity
     */
    private void workWhenLoginSucceed(){

        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if(msg.arg1==1){
                    /*对自己及lover信息进行操作*/

                    ArrayList<UserBean> userBeans=(ArrayList<UserBean>)msg.obj;

                    UserBean myBean=userBeans.get(0);
                    UserBean loverBean=userBeans.get(1);

                    /**
                     * 数据库名已用户id+.db的形式进行命名
                     */
                    UserDBHelper helper=new UserDBHelper(LoginActivity.this,myBean.getId()+".db");

                    DBManager dbManager=new DBManager(helper);

                    /**
                     * 对自己的信息进行操作
                     */
                    if(dbManager.getBeans(new UserBean(),"user_info","username=?",new String[]{myBean.getUsername()}).size()==0){
                        /*不存在则插入*/

                        dbManager.insertBean("user_info",myBean);
                    }else {
                        /*存在则更新*/

                        dbManager.updateBean(myBean,"user_info","username=?",new String[]{myBean.getUsername()});
                    }

                    /**
                     * 对lover的信息进行操作
                     */
                    if(dbManager.getBeans(new UserBean(),"user_info","username=?",new String[]{loverBean.getUsername()}).size()==0){
                        /*不存在则插入*/

                        dbManager.insertBean("user_info", loverBean);

                    }else {
                        /*存在则更新*/

                        dbManager.updateBean(loverBean,"user_info","username=?",new String[]{loverBean.getUsername()});
                    }

                    dbManager.close();

                    /**
                     * 将用户名和密码,用户id,lover用户名及是否自动登录添加到loginPreference中
                     */
                    if(remenberCheckBox.isChecked()){
                        editor.clear();
                        editor.commit();

                        editor.putString("username",username);

                        /**
                         * 对密码进行加密保存
                         */
                        editor.putString("password",new BlowfishUtil(ServerConf.PASSWORD_KEY).encryptString(password));
                        editor.commit();
                    }

                    if(autologinCheckBox.isChecked()){
                        editor.clear();
                        editor.commit();

                        editor.putString("username",username);

                        /**
                         * 对密码进行加密保存
                         */
                        editor.putString("password",new BlowfishUtil(ServerConf.PASSWORD_KEY).encryptString(password));
                        editor.putInt("userid",myBean.getId());
                        editor.putBoolean("antologin",true);
                        editor.commit();
                    }

                    /**
                     * 切换到MainActivity，并finish本activity，这里同时还要携带自己的用户名和密码及id过去
                     */
                    Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                    intent.putExtra("username",myBean.getUsername());
                    intent.putExtra("password",password);
                    intent.putExtra("userid",myBean.getId());
                    startActivity(intent);
                    finish();

                }else if(msg.arg1==2){
                    /**
                     * 进行添加lover操作
                     */
                    AddLoverDialog addLoverDialog = new AddLoverDialog(LoginActivity.this, username, password);
                    addLoverDialog.show();
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                GetUserInfoFromServer getUserInfoFromServer=new GetUserInfoFromServer();

                Message message=Message.obtain();

                if(getUserInfoFromServer.isLoverAdded(username)){
                    /*已添加lover*/

                    message.arg1=1;

                    /**
                     * 获取自己及lover信息
                     */
                    ArrayList<UserBean> userBeans=new ArrayList<UserBean>();
                    UserBean myBean=getUserInfoFromServer.getUserInfo(username);
                    UserBean loverBean=getUserInfoFromServer.getUserInfo(myBean.getLoverName());
                    userBeans.add(myBean);
                    userBeans.add(loverBean);

                    message.obj=userBeans;

                }else {
                    /*未添加lover*/

                    message.arg1 = 2;
                }

                handler.sendMessage(message);
            }
        }).start();
    }
}