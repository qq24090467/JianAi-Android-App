package com.vibexie.jianai.Utils;

import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;
import java.util.List;

/**
 *  定义栈，利用单例模式管理Activity，
 *
 *  注意如果使用了这个类，务必在项目中所有的acivity中调用下列代码
 *  在activity的onCreate()方法中调用ActivityStackManager.getInstance().push(this);
 *             onDestroy()方法中调用ActivityStackManager.getInstance().pop(this);
 *
 *  在退出时，调用ActivityStackManager.getInstance().exitApp()方法，就可以完全退出应用程序了。
 *
 *  Created by vibexie on 4/24/15.
 */
public class ActivityStackManager extends Application{

    /**
     * 构建空栈
     */
    private List<Activity> activityStack=new LinkedList<Activity>();

    private static ActivityStackManager instance;

    /**
     * 单例模式
     */
    private ActivityStackManager(){

    }

    /**
     * 获取单例
     * @return
     */
    public static ActivityStackManager getInstance(){

        if(null==instance){
            instance=new ActivityStackManager();
        }

        return instance;
    }

    /**
     * 获取activityList
     * @return
     */
    public List<Activity> getActivityStack() {
        return activityStack;
    }

    /**
     * 压入activity到activityStack中
     * @param activity
     */
    public void push(Activity activity){
        activityStack.add(activity);
    }

    /**
     * 从activityStack弹出栈顶元素
     */
    public void pop(){
        /**
         * 如果已经到了栈底,显然需要退出程序
         */
        boolean exitFlag=false;

        if(activityStack.size()==0){
            /*栈为空*/

            return;
        }else if(activityStack.size()==1) {
            /*到了栈底，应该退出App*/

            exitFlag=true;
        }

        /**
         * finish栈顶activity
         */
        activityStack.get(activityStack.size()-1).finish();

        /**
         * 在栈中移除栈顶
         */
        activityStack.remove(activityStack.size()-1);

        /**
         * 退出App
         */
        if(exitFlag){
            System.exit(0);
        }
    }

    /**
     * 弹出多个activity
     * @param num   弹出activity的数量
     */
    public void pop(int num){
        /**
         * 如果参数大于栈中activity数量，则num置为栈中activity数量
         */
        num=(activityStack.size()<num?activityStack.size():num);

        for(;num>0;num--){
            /**
             * 调用无参pop()弹出栈顶;
             */
            pop();
        }
    }

    /**
     * 从activityStack中弹出指定activity,注意在调用这个方法前不要调用finish()
     * @param activity
     */
    public void pop(Activity activity){
        /**
         * 这个判断是防止用户调用了finish()方法后再次调用这个方法，这将导致空指针异常
         */
        if(!activityStack.contains(activity)){
            return;
        }

        /**
         * finish activity
         */
        activity.finish();

        /**
         * 从activityList移除activity
         */
        activityStack.remove(activity);
    }

    /**
     * 退出APP
     */
    public void exitApp(){
        /**
         * 遍历地finish activity
         */
        for(Activity activity:activityStack){
            activity.finish();
        }

        System.exit(0);
    }
}