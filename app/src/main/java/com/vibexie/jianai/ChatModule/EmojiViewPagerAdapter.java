package com.vibexie.jianai.ChatModule;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.List;

/**
 * 显示表情的ViewPager适配器
 * Created by vibexie on 4/26/15.
 */
public class EmojiViewPagerAdapter extends PagerAdapter {
    /**
     * 显表情的页表,注意内部是一个GridView对象
     */
    private List<GridView> viewList;

    /**
     * 构造方法
     * @param viewList
     */
    public EmojiViewPagerAdapter(List<GridView> viewList){
        this.viewList=viewList;
    }

    /**
     * 获取要滑动的pager的数量
     * @return
     */
    @Override
    public int getCount() {
        if(viewList!=null){
            return viewList.size();
        }

        return 0;
    }

    /**
     * 当要显示的图片可以进行缓存的时候，会调用这个方法进行显示图片的初始化，我们将要显示的ImageView加入到ViewGroup中，然后作为返回值返回即可
     * @param container
     * @param position
     * @return
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(viewList.get(position),0);
        return viewList.get(position);
    }


    /**
     * 来判断显示的是否是同一张图片，这里我们将两个参数相比较返回即可
     * @param view
     * @param object
     * @return
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    /**
     * PagerAdapter只缓存三张要显示的图片，如果滑动的图片超出了缓存的范围，就会调用这个方法，将图片销毁
     * @param container
     * @param position
     * @param object
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(viewList.get(position));
    }
}