package com.vibexie.jianai.MainActivityModule;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by vibexie on 4/15/15.
 */
public class SlidingView extends HorizontalScrollView{

    /** 提示：以下变量设置为了static说明这个变量再内部类中需要使用，这里根据需要设置static,内部类中不使用的变量，不设置为static**/

    /**
     * slidingView对象代表整个HorizontalScrollView
     * 这个SlidingView对象是供静态的内部类的InnerClassOfSlidingView使用
     * 使的内部类方法可以整体地移动HorizontalScrollView也就是整个界面
     * slidingView在构造函数中将被赋予this
     */
    private static SlidingView slidingView;

    /**
     * 大的LinearLayout,用与容纳左布局，主布局，右布局
     */
    private LinearLayout container;

    /**
     * 左布局vieGroup，主界面向左滑动的布局，用于显示设置信息
     */
    private ViewGroup slidingLeft;

    /**
     * 中间布局viewGroup，显示主界面
     */
    private static ViewGroup slidingCenter;

    /**
     * 中间布局vieGroup与右布局vieGroup之间的消息提醒布局
     */
   private  static ViewGroup slidingRemind;

    /**
     * 右布局vieGroup，主界面向左滑动的布局，是显示消息的界面
     */
    private static ViewGroup slidingRight;

    /**
     *  屏幕宽度
     */
    private static int screenWidth;

    /**
     * 屏幕高度
     */
    private int screenHeigth;

    /**
     * 显示slidingLeft时，主界面在右边显示的宽度
     */
    private int slidingLeftPadding;

    /**
     * slidingLeft的宽度
     */
    private static int slidingLeftWidth;

    /**
     * slidingRemind的宽度，高度默认为屏幕高度
     */
    private static int slidingRemindWidth;

    /**
     * 控制onMeasure方法调用一次
     */
    private boolean hasCalledOnMeasure=false;

    /**
     * 现在是否显示slidingLeft
     */
    private boolean isSlidingLeftVisual=false;

    /**
     * 现在是否显示slidingRemind
     */
    private static boolean isSlidingRemindVisual=false;

    /**
     * 现在是否显示slidingRight
     */
    private static boolean isSlidingRightVisual=false;


    /**
     * 为使用自定义属性时，调用两个参数的构造方法
     * @param context
     * @param attrs
     */
    public SlidingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        /**
         * 将this赋予slidingView
         */
        slidingView=this;

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

//        /**
//         * 用TypedValue将数字转化为px。
//         */
//        int theValueToPix=100;
//        slidingLeftPadding=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,theValueToPix,context.getResources().getDisplayMetrics());
        /**
         * 设置slidingLeftPadding
         * 为了更好的适应不同的屏幕，不适用上面注释的方法，slidingLeftPadding不使用固定的值，而使用屏幕宽度的1/4
         */
        slidingLeftPadding=screenWidth/4;

    }

    /**
     * 测量childView的宽高以及模式，并设置自己的宽和高
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /**
         * 保证onMeasure方法调用一次
         */
        if(!hasCalledOnMeasure){
            /**
             * 自定义viewGroup内部的容器布局也就是大的LinearLayout,用与容纳左布局，主布局，右布局
             */
            container=(LinearLayout)getChildAt(0);

            /**
             * container中的第一个布局，也就是sliding_left
             */
            slidingLeft=(ViewGroup)container.getChildAt(0);

            /**
             * container中的第二个布局，也就是sliding_center
             */
            slidingCenter=(ViewGroup)container.getChildAt(1);

            /**
             * container中的第三个布局，也就是slidingRemind
             */
            slidingRemind=(ViewGroup)container.getChildAt(2);

            /**
             * container中的第四个布局，也就是sliding_right
             */
            slidingRight=(ViewGroup)container.getChildAt(3);

            /**
             * 设置sliding_left的宽度
             */
            slidingLeftWidth=slidingLeft.getLayoutParams().width=screenWidth-slidingLeftPadding;

            /**
             * 设置sliding_center的宽度,也就是屏幕的宽度
             */
            slidingCenter.getLayoutParams().width=screenWidth;

            /**
             * 设置slidingRemind的宽度,也就是屏幕的宽度的1/8
             */
            slidingRemindWidth=slidingRemind.getLayoutParams().width=screenWidth/8;


            /**
             * 设置sliding_right的宽度,也就是屏幕的宽度
             */
            slidingRight.getLayoutParams().width=screenWidth;

            /**
             * 标记已经调用过onMesure()
             */
            hasCalledOnMeasure=true;
        }

        /**这里的疑问就是为什么一定得调用super.onMeasure(widthMeasureSpec, heightMeasureSpec);没有setMeasuredDimension居然也可以，这和官方guide不同啊**/
        /**
         * override了onMeasure方法，必须要在这里调用setMeasuredDimension，设置viewGroup最终的宽和高
         * 这里viewGroup的宽度是sliding_left加上slidingLeftPadding,sliding_center，sliding_right宽度的和。也就是3个屏幕宽度
         * 高度就是屏幕的高度
         */
        setMeasuredDimension(screenWidth*3,screenHeigth);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     *  对其所有childView进行定位,即设置所有子view的大小及位置。
     * @param changed   This is a new size or position for this view
     * @param l         Left position, relative to parent
     * @param t         Top position, relative to parent
     * @param r         Right position, relative to parent
     * @param b         Bottom position, relative to parent
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        /**
         * 要将进入MainActivity时默认显示sliding_center，所以将将整个HorizontalScrollView右移slidingLeftWidth宽度即可
         */
        if(changed){
           this.scrollTo(slidingLeftWidth,0);
        }
    }

    /**
     * 监听屏幕触摸事件
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        /**
         * 实现slidingRight抽屉效果必须要这个判断
         */
        if(getScrollX()>(screenWidth+slidingLeftWidth+slidingRemindWidth)){
            this.smoothScrollTo(screenWidth+slidingLeftWidth,0);
            return true;
        }

        /**
         * 手指抬起事件
         */
        if(ev.getAction()==MotionEvent.ACTION_UP){
            /**
             * getScrollX()获取当前屏幕左上角相对于整个HorizontalScrollView左上角和x方向距离，单位px
             */
            int scrollX=getScrollX();

            if(scrollX<=(slidingLeftWidth/2)){
                /**
                 * 显示slidingLeft
                 */
                this.smoothScrollTo(0,0);

                isSlidingLeftVisual=true;

            }else if(scrollX<=slidingLeftWidth+screenWidth/2) {
                /**
                 * 显示slidingCenter
                 */
                this.smoothScrollTo(slidingLeftWidth,0);

                isSlidingLeftVisual=false;
                isSlidingRightVisual=false;
                isSlidingRemindVisual=false;

            }else{
                /**
                 * 显示slidingRemind和slidingRight,注意显示了提示，必须解决slidingCenter的焦点问题，在onScrollChanged
                 * 中已经完美解决焦点冲突问题
                 */
                this.smoothScrollTo(slidingLeftWidth+screenWidth,0);

                slidingCenter.clearFocus();
                slidingRight.requestFocus();

                isSlidingRemindVisual=true;
                isSlidingRightVisual=true;

            }

            /**
             * 在if中结束，必须返回true
             */
            return true;
        }

        return super.onTouchEvent(ev);
    }

    /**
     * 这里时特效处理
     *监听scroll事件,l为getScrollX,t为getScrollY,old顾名思意为之前的值
     * @param l
     * @param t
     * @param oldl
     * @param oldt
     */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        /**
         * 特效处理
          */
        if(l<=slidingLeftWidth){
            /**
             * slidingLeft的特效处理，注意这里的特效处理需要加入nineoldandroids.jar这个包
             */

            float scale=(float)(l/(slidingLeftWidth*1.0));
            /**
             * 右滑，主界面的缩放效果,从1放到0.8
             */
            float slidingCenterScale=(float)(0.8+0.2*scale);
            ViewHelper.setPivotX(slidingCenter,l);
            ViewHelper.setPivotY(slidingCenter,slidingCenter.getHeight()/2);
            ViewHelper.setScaleX(slidingCenter,slidingCenterScale);
            ViewHelper.setScaleY(slidingCenter,slidingCenterScale);

            /**
             * 右滑，menu的缩放效果,从0.5放到1
             */
            float slidingLeftScale=(float)(0.5+0.5*(1-scale));
            ViewHelper.setScaleX(slidingLeft,slidingLeftScale);
            ViewHelper.setScaleY(slidingLeft,slidingLeftScale);
            ViewHelper.setAlpha(slidingLeft,slidingLeftScale);


        }else if(l>slidingLeftWidth && l<(slidingLeftWidth+screenWidth)){
            /**
             * slidingRight的特效处理,透明度由0到1，右边的抽屉效果不好实现，下次研究
             */
            float scale=(float)((l-slidingLeftWidth)/(screenWidth*1.0));
            ViewHelper.setAlpha(slidingRight,scale);

            /**
             * slidingRemind和slidingRight抽屉效果
             */
            ViewHelper.setTranslationX(slidingCenter,(l-slidingLeftWidth));

            /**
             * 完美解决slidingCenter与slidingRight的焦点冲突
             */
            slidingCenter.setVisibility(View.VISIBLE);

        }else if(l>=(slidingLeftWidth+screenWidth)){

            /**
             * 完美解决slidingCenter与slidingRight的焦点冲突
             */
            slidingCenter.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 这是一个技巧
     * 外部可以通过new这个内部类，调用内部类的方法控制HorizontalScrollView的滑动
     * 完美解决activity点击按钮控制滑动的问题
     * 当然也可以通过反射来解决，不过比这个麻烦，这里选择这种方式
     */
    public static class InnerClassOfSlidingView{
        public static void toggleOfSlidingLeft(){
            if(!slidingView.isSlidingLeftVisual){
                /**
                 * 现在显示的是sliding_center,则切换到sliding_left
                 */
                slidingView.smoothScrollTo(0,0);

                slidingView.isSlidingLeftVisual=true;
            }else {
                /**
                 * 现在显示的是sliding_left,则切换到sliding_center
                 */
                slidingView.smoothScrollTo(slidingView.slidingLeftWidth,0);

                slidingView.isSlidingLeftVisual=false;
            }
        }

        public static void toggleOfSlidingRight(){
            if(!slidingView.isSlidingRightVisual){
                /**
                 * 现在显示的是sliding_center,则切换到sliding_left
                 */
                slidingView.smoothScrollTo(slidingView.slidingLeftWidth+slidingView.screenWidth,0);

                slidingView.isSlidingRightVisual=true;
            }else {
                /**
                 * 现在显示的是sliding_left,则切换到sliding_center
                 */
                slidingView.smoothScrollTo(slidingView.slidingLeftWidth,0);

                slidingView.isSlidingRightVisual=false;
            }
        }

        /**
         * 控制消息提醒的开关，当然这里只用到了打开，关闭功能暂时用不上
         */
        public static void toggleOfSlidingRemind(){
            if(!isSlidingRemindVisual && !isSlidingRightVisual){
                /*
                 *这一行代码相当简洁
                 **/

                slidingView.smoothScrollTo(slidingLeftWidth+slidingRemindWidth,0);

                isSlidingRemindVisual=true;
            }else {
                slidingView.smoothScrollTo(slidingLeftWidth,0);

                isSlidingRemindVisual=false;
            }
        }
    }
}