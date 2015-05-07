package com.vibexie.jianai.ChatModule;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * 下拉刷新listView，下拉刷新模块最后来做
 * Created by vibexie on 4/28/15.
 */
public class PullToRefreshListView  extends ListView{
    /**
     * 定义几种状态
     */
    private final static int RELEASE_To_REFRESH = 0;
    private final static int PULL_To_REFRESH = 1;
    private final static int REFRESHING = 2;
    private final static int DONE = 3;
    private final static int LOADING = 4;

    /**
     * 当前状态
     */
    private int curState;

    /**
     * listView head的宽度
     */
    private int headWidth;

    /**
     * listView head的高度
     */
    private int headHeigth;

    /**
     * 构造方法
     * @param context
     * @param attrs
     */
    public PullToRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
