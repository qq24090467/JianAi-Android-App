package com.vibexie.jianai.LoginRegisterModule;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.vibexie.jianai.Utils.ActivityStackManager;
import com.vibexie.jianai.Constants.RegisterAndRegisterCmd;
import com.vibexie.jianai.Constants.ServerConf;
import com.vibexie.jianai.MainActivityModule.MainActivity;
import com.vibexie.jianai.R;
import com.vibexie.jianai.Utils.NetworkSettingsAlertDialog;
import com.vibexie.jianai.Utils.BlowfishUtils.BlowfishUtil;
import com.vibexie.jianai.Utils.HttpClientUtil;
import com.vibexie.jianai.Utils.NetworkStateUtil;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

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
        remenberCheckBox = (CheckBox) this.findViewById(R.id.remenber);
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

                if(!isAllEditTextEdited()){
                    /*用户名和密码未输入，直接返回*/

                    return;
                }

                if(!NetworkStateUtil.isNetWorkConnected(getApplicationContext())){
                    /*网络未连接,提示用户设置网络*/

                    NetworkSettingsAlertDialog networkSettingsAlertDialog=new NetworkSettingsAlertDialog(LoginActivity.this);
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
                Handler handler=new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        /**
                         * 请求结果
                         */
                        String result=msg.obj.toString();

                        if(result.equals("refused")){
                                    /*请求超时或服务器关闭*/
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "请求超时或服务器关闭", Toast.LENGTH_SHORT).show();

                        }else if(result.equals(RegisterAndRegisterCmd.RESPONSE_LOGIN_SUCCESS)){
                            /*登录成功*/

                            progressDialog.dismiss();

                            /**
                             * 切换到MainActivity，并finish本activity
                             */
                            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(intent);
                            finish();

                        }else if(result.equals(RegisterAndRegisterCmd.RESPONSE_LOGIN_PASSWORD_WRONG)){
                            /*密码错误*/

                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "密码错误", Toast.LENGTH_SHORT).show();

                        }else if(result.equals(RegisterAndRegisterCmd.RESPONSE_LOGIN_USER_NOT_EXIST)){
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
                Intent registerIntent=new Intent(LoginActivity.this,RegisterForVerifyingCodeActivity.class);
                startActivity(registerIntent);
            }
        });
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
}