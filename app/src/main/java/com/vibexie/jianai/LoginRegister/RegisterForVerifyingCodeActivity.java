package com.vibexie.jianai.LoginRegister;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vibexie.jianai.Utils.ActivityStackManager;
import com.vibexie.jianai.Constants.RegisterAndRegisterCmd;
import com.vibexie.jianai.Constants.ServerConf;
import com.vibexie.jianai.R;
import com.vibexie.jianai.Utils.NetworkSettingsAlertDialog;
import com.vibexie.jianai.Utils.HttpClientUtil;
import com.vibexie.jianai.Utils.NetworkStateUtil;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class RegisterForVerifyingCodeActivity extends Activity {
    /**
     * 声明所有的控件
     */
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private EditText emailEditText;
    private EditText verifyingCodeEditText;
    private Button getVerifyingCodeButton;
    private Button resetButton;
    private Button continueRegisterButton;
    private ProgressDialog progressDialog;

    /**
     * 声明从EditText控件中得到的数据
     */
    private String username;
    private String password;
    private String confirmPassword;
    private String email;
    private String verifyingCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_for_verifying_code);

        /**
         * 压入到ActivityStack中
         */
        ActivityStackManager.getInstance().push(this);

        /**
         * 初始化所有的控件，并设置EditText控件的监听事件
         */
        initWidgets();

        /**
         * 初始化button点击监听事件
         */
        initClickListeners();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /**
         * 从ActivityStack中弹出
         */
        ActivityStackManager.getInstance().pop(this);
    }

    /**
     * 初始化所有的控件，并设置EditText控件的监听事件
     */
    private void initWidgets(){
        usernameEditText=(EditText)this.findViewById(R.id.username);
        passwordEditText=(EditText)this.findViewById(R.id.password);
        confirmPasswordEditText=(EditText)this.findViewById(R.id.confirmPassword);
        emailEditText=(EditText)this.findViewById(R.id.email);
        verifyingCodeEditText=(EditText)this.findViewById(R.id.verifyingCode);
        getVerifyingCodeButton=(Button)this.findViewById(R.id.getVerifyingCode);
        resetButton=(Button)this.findViewById(R.id.reset);
        continueRegisterButton=(Button)this.findViewById(R.id.continueRegister);

        username=usernameEditText.getText().toString();
        password=passwordEditText.getText().toString();
        confirmPassword=confirmPasswordEditText.getText().toString();
        email=emailEditText.getText().toString();
        verifyingCode=verifyingCodeEditText.getText().toString();

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("温馨提示");
        progressDialog.setCancelable(false);

        /**
         * 设置emailEditText焦点改变监听事件，即emailEditText一获取焦点，getVerifyingCodeButton文本恢复
         */
        emailEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    /*获得焦点*/

                    getVerifyingCodeButton.setText("获取验证码");
                }

            }
        });

        /**
         * 设置confirmPasswordEditText焦点改变监听事件，即confirmPasswordEditText失去焦点立即判断密码和确认密码是否相同
         */
        confirmPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    /*失去焦点，判断密码和确认密码是否相同*/

                    if(!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString())){
                        /*密码和确认密码不同*/

                        confirmPasswordEditText.setText("");
                        Toast.makeText(RegisterForVerifyingCodeActivity.this,"密码不一致",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        /**
         * 设置confirmPasswordEditText焦点改变监听事件,即邮箱格式是否为：xxx@xxx.xxx
         */
        emailEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    /*该EditText失去焦点，判断email格式是否正确*/

                    String tmp=emailEditText.getText().toString();
                    String[] tmp2=tmp.split("@");

                    if(tmp2.length!=2 || tmp2[1].split("\\.").length!=2){
                        /*格式不是xxx@xxx.xxx的格式*/

                        Toast.makeText(getApplicationContext(),"邮箱格式不正确",Toast.LENGTH_SHORT).show();
                        emailEditText.setText("");
                    }

                }
            }
        });
    }


    /**
     * 初始化button点击监听事件
     */
    private void initClickListeners(){

        /**
         * 验证码按钮的监听事件
         */
        getVerifyingCodeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /**
                 * 使verifyingCodeEditText立即获取焦点
                 */
                verifyingCodeEditText.requestFocus();

                /**
                 * 判断是否已填写完毕,向服务器请求验证码,并从服务器接收数据判断验证码是否发送成功
                 */
                if(isAllEditTextEdited(false)){
                    /*已填写完毕*/

                    if(!NetworkStateUtil.isNetWorkConnected(getApplicationContext())){
                        /*网络未连接，弹出NetworkSettingsAlertDialog要求用户开启网络*/

                        NetworkSettingsAlertDialog networkSettingsAlertDialog=new NetworkSettingsAlertDialog(RegisterForVerifyingCodeActivity.this);
                        networkSettingsAlertDialog.show();

                    }else {

                        /**
                         * 构建请求url
                         */
                        String url = ServerConf.REGISTER_SERVLET_URL;

                        /**
                         * 构建url请求参数
                         */
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("cmd", RegisterAndRegisterCmd.REQUEST_VERIFING_CODE));
                        params.add(new BasicNameValuePair("username", username));
                        params.add(new BasicNameValuePair("email", email));

                        /**
                         * 在handler中获取并处理结果
                         */
                        Handler handler = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                String result = (String) msg.obj;

                                if(result.equals("refused")){
                                    /*请求超时或服务器关闭*/

                                    progressDialog.dismiss();
                                    Toast.makeText(RegisterForVerifyingCodeActivity.this, "请求超时或服务器关闭", Toast.LENGTH_SHORT).show();

                                }else if (result.equals(RegisterAndRegisterCmd.RESPONSE_USERNAME_EXIST)) {
                                    /*用户名已被注册*/

                                    progressDialog.dismiss();
                                    Toast.makeText(RegisterForVerifyingCodeActivity.this, "该用户名已被注册，请更换用户名", Toast.LENGTH_SHORT).show();

                                } else if (result.equals(RegisterAndRegisterCmd.RESPONSE_EMAIL_EXIST)) {
                                    /*该邮箱已经注册*/

                                    progressDialog.dismiss();
                                    Toast.makeText(RegisterForVerifyingCodeActivity.this, "该邮箱已被注册，请更换邮箱", Toast.LENGTH_SHORT).show();
                                    emailEditText.setText("");

                                } else if (result.equals(RegisterAndRegisterCmd.RESPONSE_EMAIL_SEND_SUCCESS)) {
                                    /*邮件发送成功*/

                                    progressDialog.dismiss();
                                    getVerifyingCodeButton.setText("已发送验证码");
                                    Toast.makeText(RegisterForVerifyingCodeActivity.this, "邮件发送成功，请查收", Toast.LENGTH_SHORT).show();

                                } else if (result.equals(RegisterAndRegisterCmd.RESPONSE_EMAIL_SEND_FAIL)) {
                                    /*邮件发送失败*/

                                    progressDialog.dismiss();
                                    Toast.makeText(RegisterForVerifyingCodeActivity.this, "服务器故障,邮件发送失败", Toast.LENGTH_SHORT).show();

                                }
                            }
                        };

                        progressDialog.setMessage("正在请求验证码...");
                        progressDialog.show();

                        /**
                         * 调用请求
                         */
                        HttpClientUtil.doPost(url, params, handler);
                    }
                }
            }
        });

        /**
         * 监听重置按钮事件
         */
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 请空所有EditText
                 */
                usernameEditText.setText("");
                passwordEditText.setText("");
                confirmPasswordEditText.setText("");
                emailEditText.setText("");
                verifyingCodeEditText.setText("");

                /**
                 * 获取验证码按钮文本重新设置
                 */
                getVerifyingCodeButton.setText("获取验证码");
            }
        });

        /**
         * 监听继续注册按钮监听事件
         */
        continueRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAllEditTextEdited(true)) {
                    /*EditText已经全部输入，继续执行*/

                    /**
                     * 构建请求url
                     */
                    String url=ServerConf.REGISTER_SERVLET_URL;

                    /**
                     * 构建url请求参数
                     */
                    List<NameValuePair> params=new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("cmd", RegisterAndRegisterCmd.REQUEST_MATCH_VERIFYING_CODE));
                    params.add(new BasicNameValuePair("email",email));
                    params.add(new BasicNameValuePair("verifyingCode",verifyingCode));

                    /**
                     * 在handler中获取并处理结果
                     */
                    Handler handler=new Handler(){
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            String result=(String)msg.obj;

                            if(result.equals("refused")){
                                /*请求超时或服务器关闭*/

                                progressDialog.dismiss();
                                Toast.makeText(RegisterForVerifyingCodeActivity.this, "请求超时或服务器关闭", Toast.LENGTH_SHORT).show();

                            }else if(result.equals(RegisterAndRegisterCmd.RESPONSE_MATCH_VERIFYING_CODE_OK)){
                                /*验证码匹配成功*/

                                progressDialog.dismiss();

                                /**
                                 * 跳转到RegisterInfoActivity填写更多信息，同时将username,password,email传递过去
                                 */
                                Intent intent=new Intent(RegisterForVerifyingCodeActivity.this,RegisterInfoActivity.class);
                                intent.putExtra("username",username);
                                intent.putExtra("password",password);
                                intent.putExtra("email",email);
                                startActivity(intent);

                            }else if(result.equals(RegisterAndRegisterCmd.RESPONSE_MATCH_VERIFYING_CODE_FAIL)){
                                /*验证码匹配失败*/

                                progressDialog.dismiss();
                                Toast.makeText(RegisterForVerifyingCodeActivity.this,"验证码错误,请重新输入或再次获取",Toast.LENGTH_SHORT).show();

                            }
                        }
                    };

                    progressDialog.setMessage("正在验证您的验证码...");
                    progressDialog.show();

                    /**
                     * 调用请求
                     */
                    HttpClientUtil.doPost(url, params,handler);

                }
            }
        });
    }


    /**
     * 所有的输入控件是否已经填写
     * @param shouldJudgeVerifyingCode 是否需要判断验证码是否已经填写，如果是getVerifyingCodeButton按钮监听则不需要判断，即false
     * @return
     */
    private boolean isAllEditTextEdited(boolean shouldJudgeVerifyingCode){
        /**
         * 从控件中获取值
         */
        username=usernameEditText.getText().toString();
        password=passwordEditText.getText().toString();
        confirmPassword=confirmPasswordEditText.getText().toString();
        email=emailEditText.getText().toString();
        verifyingCode=verifyingCodeEditText.getText().toString();

        /**
         * 未填写该EditText直接返回false
         */
        if(username.equals("")){
            Toast.makeText(RegisterForVerifyingCodeActivity.this,"请填写用户名",Toast.LENGTH_SHORT).show();
            return false;
        }

        if(password.equals("")){
            Toast.makeText(RegisterForVerifyingCodeActivity.this,"请填写密码",Toast.LENGTH_SHORT).show();
            return false;
        }

        if(confirmPassword.equals("")) {
            Toast.makeText(RegisterForVerifyingCodeActivity.this, "请填写确认密码", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(email.equals("")){
            Toast.makeText(RegisterForVerifyingCodeActivity.this,"请填写邮箱",Toast.LENGTH_SHORT).show();
            return false;
        }

        /**
         * 需要判断验证码是否已经填写
         */
        if(shouldJudgeVerifyingCode){
            if(verifyingCode.equals("")){
                Toast.makeText(RegisterForVerifyingCodeActivity.this,"请填写验证码",Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }
}