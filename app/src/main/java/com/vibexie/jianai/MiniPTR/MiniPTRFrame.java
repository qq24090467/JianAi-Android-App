package com.vibexie.jianai.MiniPTR;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vibexie.jianai.R;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 下拉刷新的主框架
 * @author vibexie.com
 * @version 1.0
 */
public class MiniPTRFrame extends RelativeLayout implements OnTouchListener {

    /**
     * 下拉刷新
     */
    public static final int PULL_TO_REFRESH = 0;

    /**
     * 释放刷新
     */
    public static final int RELEASE_TO_REFRESH = 1;

    /**
     * 正在刷新
     */
    public static final int REFRESHING = 2;

    /**
     * 当前状态
      */
    private int state = PULL_TO_REFRESH;

    /**
     * 刷新回调接口
     */
    private MiniPTROnRefreshListener mListener;

    /**
     * 刷新成功还是失败
     */
    private int FRESHFLAG;

    /**
     * 刷新成功
     */
    public static final int REFRESH_SUCCEED = 0;

    /**
     * 刷新失败
     */
    public static final int REFRESH_FAIL = 1;

    /**
     * 下拉头布局
     */
    private View headView;

    /**
     * 内容布局
     */
    private View contentView;

    /**
     * 按下Y坐标，上一个事件点Y坐标
     */
    private float downY, lastY;

    /**
     * 下拉的距离
     */
    public float moveDeltaY = 0;

    /**
     * 释放刷新的距离
     */
    private float refreshDist = 200;

    /**
     * Timer
     */
    private Timer timer;
    private MyTimerTask mTask;

    /**
     * 回滚速度
     */
    public float MOVE_SPEED = 8;

    /**
     * 第一次执行布局
     */
    private boolean isLayout = false;

    /**
     * 是否可以下拉
     */
    private boolean canPull = true;

    /**
     * 在刷新过程中滑动操作
     */
    private boolean isTouchInRefreshing = false;

    /**
     * 手指滑动距离与下拉头的滑动距离比，中间会随正切函数变化
     */
    private float radio = 2;

    /**
     * 下拉箭头的转180°动画
     */
    private RotateAnimation rotateAnimation;

    /**
     * 均匀旋转动画
     */
    private RotateAnimation refreshingAnimation;

    /**
     * 下拉的箭头
     */
    private View pullView;

    /**
     * 正在刷新的图标
     */
    private View refreshingView;

    /**
     * 刷新结果图标
     */
    private View stateImageView;

    /**
     * 刷新结果：成功或失败
     */
    private TextView stateTextView;

    /**
     * 是否正显示刷新结果，失败/成功
     */
    private boolean isShowingSuccessOrFail;

    /**
     * 执行自动回滚的handler
     */
    Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            /**
             * 回弹速度随下拉距离moveDeltaY增大而增大
             */
            MOVE_SPEED = (float) (8 + 5 * Math.tan(Math.PI / 2 / getMeasuredHeight() * moveDeltaY));

            if (state == REFRESHING && moveDeltaY <= refreshDist && !isTouchInRefreshing) {
                /**
                 * 正在刷新，且没有往上推的话则悬停，显示"正在刷新..."
                 */
                moveDeltaY = refreshDist;
                mTask.cancel();
            }

            /**
             * 此时可以下拉
             */
            if (canPull){
                moveDeltaY -= MOVE_SPEED;
            }

            if (moveDeltaY <= 0) {
                /**
                 * 已完成回弹
                 */
                moveDeltaY = 0;
                pullView.clearAnimation();

                /**
                 * 隐藏下拉头时有可能还在刷新，只有当前状态不是正在刷新时才改变状态
                 */
                if (state != REFRESHING){
                    changeState(PULL_TO_REFRESH);
                }

                mTask.cancel();
            }

            /**
             * 刷新整个view tree
             * @see View#requestLayout
             */
            requestLayout();
        }

    };

    /**
     * 根据刷新结果处理UI效果的handler
     */
    Handler refressHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            refreshingView.clearAnimation();
            refreshingView.setVisibility(View.GONE);

            switch (FRESHFLAG) {
                case REFRESH_SUCCEED:
                    /*刷新成功*/
                    stateImageView.setVisibility(View.VISIBLE);
                    stateTextView.setText(R.string.refresh_succeed);
                    stateImageView.setBackgroundResource(R.drawable.miniptr_success);
                    break;
                case REFRESH_FAIL:
                    /*刷新失败*/
                    stateImageView.setVisibility(View.VISIBLE);
                    stateTextView.setText(R.string.refresh_fail);
                    stateImageView.setBackgroundResource(R.drawable.miniptr_fail);
                    break;
                default:
                    break;
            }

            /**
             * 正在显示刷新结果，在onTouch()中劫持焦点，此时无法滑动AbslistView
             */
            isShowingSuccessOrFail=true;

            /**
             * 刷新结果停留1秒，在头部显示刷新成功
             */
            new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    state = PULL_TO_REFRESH;
                    hideHead();

                }
            }.sendEmptyMessageDelayed(0, 1000);

            /**
             * 由于上面handle中hideHead()动画需要时间执行，所以得比上面handle晚一些执行
             */
            new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    /**
                     * 已经显示完了刷新结果在,onTouch()中取消劫持
                     */
                    isShowingSuccessOrFail=false;
                }
            }.sendEmptyMessageDelayed(0,1200);

        }
    };

    /**
     * 三个构造方法
     * @param context
     */
    public MiniPTRFrame(Context context) {
        this(context,null);
    }

    public MiniPTRFrame(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public MiniPTRFrame(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * 设置滑动监听器
     * @param listener
     */
    public void setOnRefreshListener(MiniPTROnRefreshListener listener) {
        mListener = listener;
    }

    /**
     * 相关的初始化，注意，其它view的初始化再onLayout()中
     * @param context
     */
    private void init(Context context) {
        /**
         * Timer任务
         */
        timer = new Timer();
        mTask = new MyTimerTask(updateHandler);

        /**
         * 设置箭头旋转动画
         */
        rotateAnimation = (RotateAnimation) AnimationUtils.loadAnimation(context, R.anim.miniptr_reverse);

        /**
         * 设置刷新旋转动画
         */
        refreshingAnimation = (RotateAnimation) AnimationUtils.loadAnimation(context, R.anim.miniptr_rotating);

        /**
         * 添加匀速转动效果
         */
        LinearInterpolator lir = new LinearInterpolator();
        rotateAnimation.setInterpolator(lir);
        refreshingAnimation.setInterpolator(lir);
    }

    /**
     * 隐藏头布局
     */
    private void hideHead() {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }

        /**
         * 重复执行task达到回弹效果
         */
        mTask = new MyTimerTask(updateHandler);
        timer.schedule(mTask, 0, 5);
    }

    class MyTimerTask extends TimerTask {
        Handler handler;

        public MyTimerTask(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            handler.sendMessage(handler.obtainMessage());
        }
    }

    /**
     * 刷新成功
     */
    public void refreshSuccess(){
        FRESHFLAG=REFRESH_SUCCEED;
        refressHandler.sendEmptyMessage(0);
    }

    /**
     * 刷新失败
     */
    public void refressFail(){
        FRESHFLAG=REFRESH_FAIL;
        refressHandler.sendEmptyMessage(0);
    }

    /**
     * 对当前状态进行判断并该改变显示效果
     * @param to
     */
    private void changeState(int to) {
        state = to;

        switch (state) {
            case PULL_TO_REFRESH:
                /*下拉刷新*/

                stateImageView.setVisibility(View.GONE);
                stateTextView.setText(R.string.pull_to_refresh);
                pullView.clearAnimation();
                pullView.setVisibility(View.VISIBLE);
                break;
            case RELEASE_TO_REFRESH:
                /*释放刷新*/

                stateTextView.setText(R.string.release_to_refresh);
                pullView.startAnimation(rotateAnimation);
                break;
            case REFRESHING:
                /*正在刷新*/

                pullView.clearAnimation();
                refreshingView.setVisibility(View.VISIBLE);
                pullView.setVisibility(View.INVISIBLE);
                refreshingView.startAnimation(refreshingAnimation);
                stateTextView.setText(R.string.refreshing);
                break;
            default:
                break;
        }
    }

    /**
     * 由父控件决定是否分发触屏事件，防止事件冲突
     * @param ev
     * @return  返会true,则交给本view的onTouchEvent()处理
     *          返回false,交给这个 view 的 interceptTouchEvent 方法来决定是否要拦截这个事件，
     *              如果 interceptTouchEvent 返回 true ，也就是拦截掉了，则交给它的 onTouchEvent 来处理，
     *              如果 interceptTouchEvent 返回 false ，那么就传递给子 view ，
     *              由子 view 的 dispatchTouchEvent 再来开始这个事件的分发。
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        switch (ev.getActionMasked())/*当然ev.getAction()也是一样的*/ {
            case MotionEvent.ACTION_DOWN:
                downY = ev.getY();

                /**
                 * 记录点击触屏的Y坐标
                 */
                lastY = downY;

                if (mTask != null) {
                    mTask.cancel();
                }

                /**
                 * 按下点击的地方位于下拉头布局，由于我们没有对下拉头做事件响应，
                 * 这时候它会给我们返回一个false导致接下来的事件不再分发进来。
                 * 所以我们不能交给父类分发，直接返回true
                 * 这样这次点击的ACTION_MOVE和ACTION_UP均无效
                 */
                if (ev.getY() < moveDeltaY){
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                /**
                 * canPull这个值在底下onTouch中会根据ListView是否滑到顶部来改变，意思是是否可下拉
                 */
                if (canPull) {
                    /**
                     * 对实际滑动距离做缩小，即阻尼效果，造成用力拉的感觉
                     */
                    moveDeltaY = moveDeltaY + (ev.getY() - lastY) / radio;

                    if (moveDeltaY < 0){
                        moveDeltaY = 0;
                    }

                    if (moveDeltaY > getMeasuredHeight()){
                        moveDeltaY = getMeasuredHeight();
                    }

                    if (state == REFRESHING) {
                        /*正在刷新的时候触摸移动*/

                        isTouchInRefreshing = true;
                    }
                }

                lastY = ev.getY();

                /**
                 * 根据下拉距离改变比例
                 */
                radio = (float) (2 + 2 * Math.tan(Math.PI / 2 / getMeasuredHeight() * moveDeltaY));

                /**
                 * Call this when something has changed which has invalidated the layout of this view.
                 * 重新布局
                 */
                requestLayout();

                if (moveDeltaY <= refreshDist && state == RELEASE_TO_REFRESH) {
                    /**
                     * 如果下拉距离没达到刷新的距离且当前状态是释放刷新，改变状态为下拉刷新
                     */
                    changeState(PULL_TO_REFRESH);
                }

                if (moveDeltaY >= refreshDist && state == PULL_TO_REFRESH) {
                    changeState(RELEASE_TO_REFRESH);
                }

                if (moveDeltaY > 0) {
                    /**
                     * 防止下拉过程中误触发长按事件和点击事件
                     */
                    clearContentViewEvents();

                    /**
                     * 正在下拉，不将touch事件分发给子view
                     */
                    return true;
                }

                break;

            case MotionEvent.ACTION_UP:
                if (moveDeltaY > refreshDist){
                    /*正在刷新时往下拉释放后下拉头不隐藏*/
                    isTouchInRefreshing = false;
                }

                if (state == RELEASE_TO_REFRESH) {
                    changeState(REFRESHING);

                    /**
                     * 执行刷新所要执行的任务
                     */
                    if (mListener != null){
                        mListener.onRefresh();
                    }
                }

                /**
                 * 隐藏头部
                 */
                hideHead();

            default:
                break;

        }

        /**
         * 在这之前没有return的话，父类进行事件分发
         */
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 通过反射AbsListView修改字段去掉长按事件和点击事件
     */
    private void clearContentViewEvents() {
        try {
            Field[] fields = AbsListView.class.getDeclaredFields();
            for (int i = 0; i < fields.length; i++)
                if (fields[i].getName().equals("mPendingCheckForLongPress")) {
                    /**
                     * mPendingCheckForLongPress是AbsListView中的字段，通过反射获取并从消息列表删除，去掉长按事件
                     */
                    fields[i].setAccessible(true);
                    contentView.getHandler().removeCallbacks((Runnable) fields[i].get(contentView));

                } else if (fields[i].getName().equals("mTouchMode")) {
                    /**
                     * TOUCH_MODE_REST = -1， 这个可以去除点击事件
                     */
                    fields[i].setAccessible(true);
                    fields[i].set(contentView, -1);
                }

            /**
             * 去掉焦点
             */
            ((AbsListView) contentView).getSelector().setState(new int[]{ 0 });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 绘制contentView顶部，或者说头部布局底部的阴影效果，颜色值可以修改，这里使用了android绘图
     * @see android.view.ViewGroup#dispatchDraw(android.graphics.Canvas)
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        /**
         * 没有出现头部View，不使用该效果，也没有必要使用该效果
         */
        if (moveDeltaY == 0){
            return;
        }

        RectF rectF = new RectF(0, 0, getMeasuredWidth(), moveDeltaY);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        /**
         * 这里使用线性梯度，阴影的高度为25，最底部颜色值为0x66000000，25高度处颜色为0x00000000,高度和颜色值也可以根据需要改变
         */
        LinearGradient linearGradient = new LinearGradient(0, moveDeltaY, 0, moveDeltaY - 25, 0x66000000, 0x00000000, TileMode.CLAMP);
        paint.setShader(linearGradient);
        paint.setStyle(Style.FILL);

        /**
         * 在moveDeltaY处往上变淡
         */
        canvas.drawRect(rectF, paint);
    }

    /**
     * 重载onLayout(),给子view布局
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        /**
         * 保证初始化一次
         */
        if (!isLayout) {
            headView = getChildAt(0);
            contentView = getChildAt(1);

            /**
             * view的相关初始化
             */
            pullView = headView.findViewById(R.id.pull_icon);
            stateTextView = (TextView) headView.findViewById(R.id.state_tv);
            refreshingView = headView.findViewById(R.id.refreshing_icon);
            stateImageView = headView.findViewById(R.id.state_iv);

            /**
             * 给AbsListView设置OnTouchListener
             */
            contentView.setOnTouchListener(this);

            /**
             * 获取headView的高度，并赋值给refreshDist
             */
            refreshDist = ((ViewGroup) headView).getChildAt(0).getMeasuredHeight();
            isLayout = true;
        }

        if (canPull) {
            /**可以下拉，手动给子控件布局**/
            headView.layout(0, (int) moveDeltaY - headView.getMeasuredHeight(), headView.getMeasuredWidth(), (int) moveDeltaY);
            contentView.layout(0, (int) moveDeltaY, contentView.getMeasuredWidth(), (int) moveDeltaY + contentView.getMeasuredHeight());

        }else{
            /*不可以下拉，默认布局*/
            super.onLayout(changed, l, t, r, b);
        }
    }

    /**
     * 对dispatchTouchEvent()分发给onTouch的事件进行处理
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        /**
         * 正在显示刷新结果，劫持
         */
        if(isShowingSuccessOrFail){
            return true;
        }

        AbsListView alv = (AbsListView) v;

        if (alv.getCount() == 0) {
            /*没有item的时候也可以下拉刷新*/
            canPull = true;
        } else if (alv.getFirstVisiblePosition() == 0 && alv.getChildAt(0).getTop() >= 0) {
            /*滑到AbsListView的顶部了，即第一个item可见且滑动到顶部*/
            canPull = true;
        } else{
            canPull = false;
        }

        /**
         * 此事件交给下一级AbsListView子类进行处理
         */
        return false;
    }
}