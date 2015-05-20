package com.vibexie.jianai.LoginRegister;

import android.content.Context;
import android.widget.Toast;

import com.vibexie.jianai.Constants.RegisterAndRegisterCmd;
import com.vibexie.jianai.Constants.ServerConf;
import com.vibexie.jianai.Dao.Bean.UserBean;
import com.vibexie.jianai.Dao.Bean.UserBeanJsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vibexie on 5/19/15.
 */
public class GetUserInfoFromServer {

    Map<String,String> map=new HashMap<String,String>();

    public GetUserInfoFromServer(){

    }

    /**
     * 判断是否添加了lover
     * @return
     */
    public boolean isLoverAdded(String username){
        boolean flag=false;

        map.clear();
        map.put("cmd", RegisterAndRegisterCmd.REQUEST_USER_INFO);
        map.put("url", ServerConf.SERVER_ADDR + "JianaiServer/AddloverAndgetuserinfoServlet");

        if(map.containsKey("username")){
            map.remove("username");
        }

        map.put("username",username);

        if(getUserBean(map,"POST").getLoverName()!=null){
            flag=true;
        }

        return flag;
    }

    /**
     * 返回用户的信息
     * @return
     */
    public UserBean getUserInfo(String username){
        map.clear();
        map.put("cmd", RegisterAndRegisterCmd.REQUEST_USER_INFO);
        map.put("url", ServerConf.SERVER_ADDR + "JianaiServer/AddloverAndgetuserinfoServlet");

        if(map.containsKey("username")){
            map.remove("username");
        }

        map.put("username",username);

        return getUserBean(map, "POST");
    }

    /**
     * 获取用户信息
     * @param map
     * @param method
     * @return
     */
    public UserBean getUserBean(Map<String,String> map,String method){
        UserBean userBean=null;
        try {
            /**
             *  设置请求URL
             */
            URL url = new URL(map.remove("url"));

            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

            /**
             * 连接设置
             */
            httpURLConnection.setAllowUserInteraction(false);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setReadTimeout(20000);
            httpURLConnection.setIfModifiedSince(0);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setConnectTimeout(20000);
            httpURLConnection.setRequestMethod(method);
            httpURLConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");

            httpURLConnection.connect();

            BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream()));

            /**
             * 设置请求参数
             */
            String params="?";
            for (String key : map.keySet()) {
                params=params.concat(key+"="+map.get(key)+"&");
            }
            params=params.substring(1,params.lastIndexOf('&'));

            writer.append(params);
            writer.flush();


            InputStream inputStream = httpURLConnection.getInputStream();

            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));

            JSONObject jsonObject= null;
            try {
                jsonObject = new JSONObject(bufferedReader.readLine());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            userBean= UserBeanJsonUtil.Json2UserBean(jsonObject);

            inputStream.close();
            httpURLConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return userBean;
    }
}
