package com.vibexie.jianai.MainActivityModule;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.vibexie.jianai.Utils.StatusBarHeightUtil;

/**
 * Created by vibexie on 4/25/15.
 */
public class CenterOfSlidingView extends ScrollView {

    /**
     * 大的LinearLayout,用与容纳上布局，中布局，右布局
     */
    private LinearLayout container;

    /**
     * 上布局的ViewGroup，用于下拉打开情侣空间
     */
    private ViewGroup slidingTop;

    /**
     * 中间布局的ViewGroup，用于显示主界面
     */
    private ViewGroup slidingCenter;

    /**
     * 下布局的ViewGroup，用于上拉打开“我睡了”
     */
    private ViewGroup slidingBottom;

    /**
     *  屏幕宽度
     */
    private static int screenWidth;

    /**
     * 屏幕高度
     */
    private int screenHeigth;

    /**
     * 上布局的高度
     */
    private int slidingTopHeigth;

    /**
     * 中间布局的高度就是屏幕的高度
     */
    private int slidingCenterHeigth;

    /**
     * 下布局的高度
     */
    private int slidingBottomHeigth;

    /**
     * 控制onMeasure方法调用一次
     */
    private boolean hasCalledOnMeasure=false;

    public CenterOfSlidingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        /**
         * 持有DisplayMetrics对象以获得屏幕的宽高
         */
        WindowManager windowManager=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics=new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        /**
         * 获得屏幕的宽度
         */
        screenWidth=displayMetrics.widthPixels;

        /**
         * 获得屏幕的高度
         */
        screenHeigth=displayMetrics.heightPixels;

        //this.setVisibility(View.INVISIBLE);

    }

    /**
     * 测量childView的宽高以及模式，并设置自己的宽和高
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if(!hasCalledOnMeasure){
            /**
             * 自定义viewGroup内部的容器布局也就是大的LinearLayout,用与容纳上布局，中间布局，下布局
             */
            container=(LinearLayout)getChildAt(0);

            /**
             * container的第一个子布局，也就是上布局
             */
            slidingTop=(ViewGroup)container.getChildAt(0);

            /**
             * container的第二个子布局，也就是中间布局
             */
            slidingCenter=(ViewGroup)container.getChildAt(1);

            /**
             * container的第三个子布局，也就是下布局
             */
            slidingBottom=(ViewGroup)container.getChildAt(2);

            /**
             * 设置上布局的高度为屏幕高度的1/5
             */
            slidingTopHeigth=slidingTop.getLayoutParams().height=screenHeigth/5;

            /**
             * 设置中间布局的高度为屏幕高度-状态栏高度
             */
            slidingCenterHeigth=slidingCenter.getLayoutParams().height=screenHeigth-StatusBarHeightUtil.getStatusBarHeight(this);

            /**
             * 设置下布局的高度为屏幕高度的1/5
             */
            slidingBottomHeigth=slidingBottom.getLayoutParams().height=screenHeigth/5;

            hasCalledOnMeasure=true;
        }

        setMeasuredDimension(screenWidth,screenHeigth+slidingTopHeigth+slidingBottomHeigth);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        /**
         * 打开时立即显示的是中间布局，所以下移slidingTopHeigth高度
         */
        if(changed){
            this.smoothScrollTo(0,slidingTopHeigth);
        }
    }

    /***
     * 特效的处理：实现下拉打开“情侣空间”，上拉打开“我睡了”
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        /**
         * 手指抬起的的特效处理
         */
        if(ev.getAction()==MotionEvent.ACTION_UP){

            int scrollY=getScrollY();


            if(scrollY<slidingTopHeigth/2){
                /*立即打开“情侣空间”*/

                System.out.println("--->>立即打开“情侣空间”");

                this.smoothScrollTo(0,slidingTopHeigth);

            }if(scrollY<slidingTopHeigth){
                /*恢复正常显示，即下是slidingCenter*/

                System.out.println("--->>scrollY<slidingTopHeigth");
                this.smoothScrollTo(0,slidingTopHeigth);

            }else if(scrollY<(slidingTopHeigth+slidingBottomHeigth/2)){
                 /*恢复正常显示，即下是slidingCenter*/

                System.out.println("--->>scrollY<(slidingTopHeigth+slidingBottomHeigth/2)");
                this.smoothScrollTo(0,slidingTopHeigth);

            }else{
                /*立即打开“我睡了”*/

                System.out.println("--->>立即打开“我睡了”");
                this.smoothScrollTo(0,slidingTopHeigth);

            }

            return true;
        }

        return super.onTouchEvent(ev);
    }
}