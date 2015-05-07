package com.vibexie.jianai.Utils;

import android.os.Handler;
import android.os.Message;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * HttpClient的工具类
 * Created by vibexie on 4/17/15.
 *
 * 这个工具类可能使用起来比较麻烦，但是它解决了网络连接超时的问题，使得UI主线程不会因为网络连接时间过长而产生ANR
 */
public class HttpClientUtil {

    /**
     * post请求方法，注意这个方法里自动创建了一条线程
     * @param url       url地址
     * @param params    请求参数
     * @param handler   关联的handler
     */
    public static void doPost(final String url,final List<NameValuePair> params,final Handler handler){
/******************************        使用说明          **************************/
//
//        /**
//         * 构建请求url
//         */
//        String url=ServerConf.SERVER_ADDR+"JianaiServer/RegisterServlet";
//
//        /**
//         * 构建url请求参数
//         */
//        List<NameValuePair> params=new ArrayList<NameValuePair>();
//        params.add(new BasicNameValuePair("cmd", RegisterAndRegisterCmd.REQUEST_VERIFING_CODE));
//        params.add(new BasicNameValuePair("info","vibexie@qq.com"));
//
//        /**
//         * 构建handler,在handler中获取并处理结果
//         */
//        Handler handler=new Handler(){
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                String result=(String)msg.obj;
//                Toast.makeText(RegisterForVerifyingCodeActivity.this,result,Toast.LENGTH_SHORT).show();
//
//            }
//        };
//
//        /**
//         * 调用请求方法
//         */
//        HttpClientUtil.doPost(url, params,handler);
/******************************        使用说明          **************************/

        /**
         * 创建新线程
         */
        new Thread(new Runnable() {

            @Override
            public void run() {
                /**
                 * 返回的结果
                 */
                String result=null;

                /**
                 * 国外程序员使用的超时方案
                 */
                HttpParams httpParams=new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams,20000);
                HttpConnectionParams.setSoTimeout(httpParams,20000);

                DefaultHttpClient httpClient= new DefaultHttpClient(httpParams);

                /**
                 * 也可以这样设置超时
                 */
//                /**
//                 * 设置请求超时60秒
//                 */
//                httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,10000);
//
//                /**
//                 * 设置数据传输超时60秒
//                 */
//                httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,10000);

                HttpPost httpPost=new HttpPost(url);

                /**
                 * 设置请求参数
                 */
                UrlEncodedFormEntity entity= null;
                try {
                    entity = new UrlEncodedFormEntity(params,"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                httpPost.setEntity(entity);

                /**
                 * 执行请求并获得返回结果
                 */
                try {
                    HttpResponse httpResponse=httpClient.execute(httpPost);

                    if(httpResponse.getStatusLine().getStatusCode()==200){
                        result=EntityUtils.toString(httpResponse.getEntity());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    /**
                     * 关闭httpClient，这也是不将httpClient设置为类的静态属性的原因
                     *
                     * 特别注意，不能在解析httpResponse.getEntity()前关闭连接，这里非常容易犯错
                     */
                    httpClient.getConnectionManager().shutdown();
                }

                /**
                 * 未返回结果，请求超时或服务器关闭，置result为refused
                 */
                if(result==null){
                    result="refused";
                }

                /**
                 * 发送返回结果给handler
                 */
                Message message=Message.obtain();
                message.obj=result;
                handler.sendMessage(message);
            }
        }).start();
    }
}