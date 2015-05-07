package com.vibexie.jianai.LoginRegisterModule;

import android.app.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.vibexie.jianai.Utils.BlowfishUtils.BlowfishUtil;
import com.vibexie.jianai.Utils.HttpClientUtil;
import com.vibexie.jianai.Utils.NetworkStateUtil;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class RegisterInfoActivity extends Activity {
    /**
     * 声明所有的控件
     */
    private EditText realnameEditText;
    private EditText birthdayEditText;
    private EditText sexEditText;
    private EditText phoneEditText;
    private EditText addressEditText;
    private Button compeleteRegisterButton;
    private ProgressDialog progressDialog;

    /**
     * 声明从EditText控件中得到的数据
     */
    private String realname;
    private String birthday;
    private String sex;
    private String phone;
    private String address;

    /**
     * 声明从Intent传递过来的信息
     */
    private String username;
    /**
     * 直接将Intent传递过来的密码进行加密
     */
    private String encryptedPassword;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_info);

        /**
         * 压入到ActivityStack中
         */
        ActivityStackManager.getInstance().push(this);

        /**
         * 从Intent接收数据
         */
        initValueFromIntent();

        /**
         * 初始化所有的控件
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
     * 从Intent接收数据
     */
    private void initValueFromIntent(){
        Intent intent=this.getIntent();
        username=intent.getStringExtra("username");
        /**
         * 用BlowFish算法进行加密
         */
        encryptedPassword= new BlowfishUtil(ServerConf.PASSWORD_KEY).encryptString(intent.getStringExtra("password"));
        email=intent.getStringExtra("email");
    }

    /**
     * 初始化所有的控件
     */
    private void initWidgets(){
        realnameEditText=(EditText)this.findViewById(R.id.realname);
        birthdayEditText=(EditText)this.findViewById(R.id.birthday);
        sexEditText=(EditText)this.findViewById(R.id.sex);
        phoneEditText=(EditText)this.findViewById(R.id.phone);
        addressEditText=(EditText)this.findViewById(R.id.address);
        compeleteRegisterButton=(Button)this.findViewById(R.id.compeleteRegister);

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("温馨提示");
        progressDialog.setMessage("正在完成注册...");
        progressDialog.setCancelable(false);

        sexEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    /*失去焦点，判断性别是否合法*/

                    String tmp = sexEditText.getText().toString();
                    if (!tmp.equals("男") && !tmp.equals("女")) {
                        /*不合法*/

                        Toast.makeText(getApplicationContext(),"性别不合法",Toast.LENGTH_SHORT).show();
                        sexEditText.setText("");
                    }
                }
            }
        });

        birthdayEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    /*失去焦点，判断生日是否合法*/

                    String tmp=birthdayEditText.getText().toString();

                    String[] tmp2=tmp.split("\\.");

                    if(tmp2.length==3){
                        int year=Integer.valueOf(tmp2[0]);
                        int month=Integer.valueOf(tmp2[1]);
                        int day=Integer.valueOf(tmp2[2]);

                        if(year<10000 && year>1900 && month>0 && month<13 && day>0 && day<32){
                            /*生日格式正确*/
                        }else {
                            /*生日格式不正确*/

                            birthdayEditText.setText("");
                            Toast.makeText(getApplicationContext(),"生日格式错误",Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        /*生日格式不正确*/

                        birthdayEditText.setText("");
                        Toast.makeText(getApplicationContext(),"生日格式错误",Toast.LENGTH_SHORT).show();
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
         * 完成注册按钮的监听事件
         */
        compeleteRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAllEditTextEdited()){
                   /*用户信息已经输入*/

                   if(!NetworkStateUtil.isNetWorkConnected(getApplicationContext())){
                       /*网络未连接，弹出NetworkSettingsAlertDialog要求用户开启网络*/

                       NetworkSettingsAlertDialog networkSettingsAlertDialog=new NetworkSettingsAlertDialog(RegisterInfoActivity.this);
                       networkSettingsAlertDialog.show();

                   }else {
                       /*网络已连接，向服务器请求*/

                       /**
                        * 构建请求url
                        */
                       String url = ServerConf.REGISTER_SERVLET_URL;

                       /**
                        * 构建url请求参数
                        */
                       List<NameValuePair> params = new ArrayList<NameValuePair>();
                       params.add(new BasicNameValuePair("cmd", RegisterAndRegisterCmd.REQUEST_COMPETE_REGISTER));
                       params.add(new BasicNameValuePair("username", username));
                       params.add(new BasicNameValuePair("encryptedPassword", encryptedPassword));
                       params.add(new BasicNameValuePair("realname",realname));
                       params.add(new BasicNameValuePair("sex", sex));
                       params.add(new BasicNameValuePair("birthday", birthday));
                       params.add(new BasicNameValuePair("email", email));
                       params.add(new BasicNameValuePair("phone", phone));
                       params.add(new BasicNameValuePair("address", address));

                       /**
                        * 构建AlertDialog
                        */
                       final AlertDialog.Builder builder=new AlertDialog.Builder(RegisterInfoActivity.this);
                       builder.setCancelable(false);

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

                                   Toast.makeText(RegisterInfoActivity.this, "请求超时或服务器关闭", Toast.LENGTH_SHORT).show();

                               }else if(result.equals(RegisterAndRegisterCmd.RESPONSE_COMPETE_REGISTER_OK)) {
                                   /*成功注册用户*/

                                   progressDialog.dismiss();

                                   builder.setTitle("恭喜您注册成功");
                                   builder.setMessage("现在去登录?");
                                   builder.setPositiveButton("登录",new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int which) {
                                           /*结束2个栈元素，回到登录界面*/
                                           ActivityStackManager.getInstance().pop(2);
                                       }
                                   });

//                                   builder.setNegativeButton("退出",new DialogInterface.OnClickListener() {
//                                       @Override
//                                       public void onClick(DialogInterface dialog, int which) {
//
//                                       }
//                                   });

                                   builder.create();
                                   builder.show();

                               }else if(result.equals(RegisterAndRegisterCmd.RESPONSE_VERIFYING_CODE_OVERDUE)){
                                   /*验证码过期*/

                                   progressDialog.dismiss();

                                   builder.setTitle("验证码过期,注册失败");
                                   builder.setMessage("重新获取验证码?");
                                   builder.setPositiveButton("是的",new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                       }
                                   });

                                   builder.setNegativeButton("退出",new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int which) {
                                            ActivityStackManager.getInstance().exitApp();
                                       }
                                   });

                                   builder.create();
                                   builder.show();


                               }else if(result.equals(RegisterAndRegisterCmd.RESPONSE_COMPETE_REGISTER_FAIL)){
                                   /*注册用户失败*/

                                   progressDialog.dismiss();

                                   builder.setTitle("服务器故障,注册失败");
                                   builder.setMessage("重新提交注册信息?");
                                   builder.setPositiveButton("是的",new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int which) {
                                            /*不进行任何操作，只是关闭dialog*/
                                       }
                                   });

                                   builder.setNegativeButton("下次注册",new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int which) {
                                           ActivityStackManager.getInstance().exitApp();
                                       }
                                   });

                                   builder.create();
                                   builder.show();
                               }

                           }
                       };

                       progressDialog.show();
                       /**
                        * 调用请求
                        */
                       HttpClientUtil.doPost(url, params, handler);
                   }
                }
            }
        });
    }

    /**
     * 所有的输入控件是否已经填写
     * @return
     */
    private boolean isAllEditTextEdited(){
        /**
         * 从控件中获取值
         */
        realname=realnameEditText.getText().toString();
        birthday=birthdayEditText.getText().toString();
        sex=sexEditText.getText().toString();
        phone=phoneEditText.getText().toString();
        address=addressEditText.getText().toString();

        if(realname.equals("")){
            Toast.makeText(getApplicationContext(),"请输入您的真实姓名",Toast.LENGTH_SHORT).show();
            return false;
        }

        if(birthday.equals("")){
            Toast.makeText(getApplicationContext(),"请输入您的生日",Toast.LENGTH_SHORT).show();
            return false;
        }

        if(sex.equals("")){
            Toast.makeText(getApplicationContext(),"请输入您的性别",Toast.LENGTH_SHORT).show();
            return false;
        }

        if(phone.equals("")){
            Toast.makeText(getApplicationContext(),"请输入您的手机号",Toast.LENGTH_SHORT).show();
            return false;
        }

        if(address.equals("")){
            Toast.makeText(getApplicationContext(),"请输入您的地址",Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}