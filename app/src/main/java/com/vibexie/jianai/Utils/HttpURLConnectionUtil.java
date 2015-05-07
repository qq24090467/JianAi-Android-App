package com.vibexie.jianai.Utils;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by vibexie on 4/24/15.
 */
public class HttpURLConnectionUtil {

    /**
     * post请求
     * @param map   请求参数:注意map必须要有一个url key
     * @return
     */
    public static byte[] doPost(Map<String,String> map){
        return doConnection(map,"POST");
    }

    /**
     * get请求
     * @param map   请求参数:注意map必须要有一个url key
     * @return
     */
    public static byte[] doGet(Map<String,String> map){
        return doConnection(map,"GET");
    }

    /**
     * 请求的实现
     * @param map
     * @param method
     * @return
     */
    public static byte[] doConnection(Map<String,String> map,String method) {
        /**
         * 返回的结果
         */
        byte[] bytes=null;

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

            bytes= new byte[inputStream.available()];

            inputStream.read(bytes);

            inputStream.close();
            httpURLConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * 返回字节数组结果
         */
        return bytes;
    }

}
