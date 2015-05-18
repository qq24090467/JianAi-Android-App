package com.vibexie.jianai.LoginRegister;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.vibexie.jianai.R;

import java.text.DecimalFormat;


/**
 * Created by vibexie on 4/2/15.
 */
public class CircleProgressBar extends View {
    /**
     *内圈的颜色
     */
    private int innerColor;
    /**
     *外圈的颜色
     */
    private int outerColor;
    /**
     *内圆的半径
     */
    private int circleRadius;
    /**
     * 圆环的宽度
     */
    private int ringWidth;
    /**
     *速度
     */
    private int speed;
    /**
     *当前进度
     */
    private int currentProgress;
    /*
     *环的画笔
     */
    private Paint circlePaint;

    /**
     * 进度画笔
     */
    private Paint progressPaint;

    /**
     * 进度文本字体大小
     */
    private int progressTextSize;

    /**
     * 显示的文本
     */
    private String text="等候输入";

    public CircleProgressBar(Context context) {
        this(context, null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        /**
         *获取自定义属性数组
         */
        TypedArray typedArray=context.obtainStyledAttributes(attrs,R.styleable.MyProgressBar);
        int attrCount=typedArray.getIndexCount();

        /**
         *提取自定义属性的值
         */
        for(int i=0;i<attrCount;i++){
            int attr=typedArray.getIndex(i);
            switch (attr){
                case R.styleable.MyProgressBar_innerColor:
                    //getColor的第二个参数是在第一个参数为空的时候设置的值
                    innerColor=typedArray.getColor(R.styleable.MyProgressBar_innerColor, Color.GREEN);
                    break;
                case R.styleable.MyProgressBar_outerColor:
                    outerColor=typedArray.getColor(R.styleable.MyProgressBar_outerColor, Color.RED);
                    break;
                case R.styleable.MyProgressBar_innerCircleRadius:
                    circleRadius=typedArray.getDimensionPixelOffset(R.styleable.MyProgressBar_innerCircleRadius,100);
                    break;
                case R.styleable.MyProgressBar_ringWidth:
                    ringWidth=typedArray.getDimensionPixelOffset(R.styleable.MyProgressBar_ringWidth, 10);
                    break;
                case R.styleable.MyProgressBar_progressTextSize:
                    progressTextSize=typedArray.getDimensionPixelSize(R.styleable.MyProgressBar_progressTextSize,25);
                    break;
                case R.styleable.MyProgressBar_speed:
                    speed=typedArray.getInt(R.styleable.MyProgressBar_speed,20);
                    break;
            }
        }

        /**
         * Recycle the TypedArray, to be re-used by a later caller. After calling
         * this function you must not ever touch the typed array again.
         */
        typedArray.recycle();

        /**
         * new出画笔
         */
        circlePaint=new Paint();
        progressPaint=new Paint();

        /**
         * 绘图线程
         */
        new Thread()
        {
            public void run()
            {
                while (true)
                {
                    currentProgress++;
                    if (currentProgress == 360)
                    {
                        currentProgress = 0;
                    }
                    postInvalidate();
                    try
                    {
                        Thread.sleep(speed);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            };
        }.start();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 获取圆心的x坐标
         */
        int x=(circleRadius+ringWidth);
        /**
         * 获取圆心的Y坐标
         */
        int y=(circleRadius+ringWidth);

        /**
         *设置环画笔的属性
         */
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(ringWidth);
        circlePaint.setAntiAlias(true);

        /**
         * 设置进度画笔的属性
         */
        progressPaint.setStyle(Paint.Style.FILL);
        progressPaint.setTextSize(progressTextSize);
        progressPaint.setAntiAlias(true);
        /**
         * 进度文本的X坐标
         */
        int progressX=(int)(x/2);
        int progressY=y+(progressTextSize/2);
        /**
         * 用于定义的圆弧的形状和大小的界限
         */
        RectF oval = new RectF(x-circleRadius, y-circleRadius, x+circleRadius, y+circleRadius);
        /**
         * 底层的圆圈完整，上层的圆圈跑
         */
        circlePaint.setColor(innerColor);
        canvas.drawCircle(x, y, circleRadius, circlePaint);
        circlePaint.setColor(outerColor);

        canvas.drawText("" + text, progressX, progressY, progressPaint);
        canvas.drawArc(oval, -90, currentProgress, false, circlePaint);
    }

    public void setText(String text){
        this.text=text;
    }
}
