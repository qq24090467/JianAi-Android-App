package com.vibexie.jianai.ChatModule;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.vibexie.jianai.Dao.Bean.ChatMsgBean;
import com.vibexie.jianai.R;
import com.vibexie.jianai.Utils.GifUtils.AnimatedGifDrawable;
import com.vibexie.jianai.Utils.GifUtils.AnimatedImageSpan;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vibexie on 4/27/15.
 */
public class ListViewAdapter extends BaseAdapter{
    private Context context;
    private List<ChatMsgBean> list;
    private LayoutInflater layoutInflater;

    /**
     * 长按消息，显示复制，删除选项
     */
    private PopupWindow popupWindow;

    /**
     * 复制和删除的textView
     */
    private TextView copyTextView,deleteTextView;

    /**
     * holder
     */
    private class ViewHolder{
        /**
         * 对方，自己的头像
         */
        ImageView friendHead,myHead;

        /**
         * 接收的，发送的消息
         */
        TextView msgContentFromFriend,msgContentFromMe,msgTime;

        /**
         * 显示接收消息，发送消息的布局
         */
        ViewGroup layout_from_friend,layout_from_me;
    }

    /**
     * 构造方法
     * @param context
     * @param list
     */
    public ListViewAdapter(Context context, List<ChatMsgBean> list) {
        this.context = context;
        this.list = list;

        layoutInflater=LayoutInflater.from(context);

        /**
         * 初始化popupWindow
         */
        View popView=layoutInflater.inflate(R.layout.activity_chat_listview_item_copy_delete,null);
        copyTextView=(TextView)popView.findViewById(R.id.listview_item_copy);
        deleteTextView=(TextView)popView.findViewById(R.id.listview_item_delete);

        popupWindow=new PopupWindow(popView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
    }

    /**
     * 更新了消息需要重新设置list
     * @param list
     */
    public void resetList(List<ChatMsgBean> list){
        this.list=list;
    }

    /**
     * 接下来是4个父类的重载方法
     */
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 这个方法最重要了
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if(convertView==null){
            viewHolder=new ViewHolder();

            /**
             * 加载布局文件
             */
            convertView=layoutInflater.inflate(R.layout.activity_chat_listview_item,null);

            viewHolder.layout_from_friend=(ViewGroup)convertView.findViewById(R.id.layout_from_friend);
            viewHolder.layout_from_me=(ViewGroup)convertView.findViewById(R.id.layout_from_me);

            viewHolder.msgContentFromFriend=(TextView)convertView.findViewById(R.id.msg_content_from_friend);
            viewHolder.msgContentFromMe=(TextView)convertView.findViewById(R.id.msg_content_from_me);

            viewHolder.msgTime=(TextView)convertView.findViewById(R.id.msg_time);

            /**
             * 设置标记
             */
            convertView.setTag(viewHolder);

        }else {
            /**
             * 从标记中获取
             */
            viewHolder=(ViewHolder)convertView.getTag();
        }

        if(list.get(position).getFromOrTo()==0){
            /*表示收到的消息*/

            /**
             * 将接收消息的布局显示，发送消息的布局隐藏
             */
            viewHolder.layout_from_friend.setVisibility(View.VISIBLE);
            viewHolder.layout_from_me.setVisibility(View.GONE);

            /**
             * 先对消息进行处理，再设置本条item的消息内容和消息时间
             */
            SpannableStringBuilder sb=handler(viewHolder.msgContentFromFriend,list.get(position).getMsgContent());
            viewHolder.msgContentFromFriend.setText(sb);
            viewHolder.msgTime.setText(list.get(position).getMsgTime());

        }else {
            /*表示发送的消息，即list.get(position).fromOrTo=1*/

            /**
             * 将发送消息的布局显示，接收消息的布局隐藏
             */
            viewHolder.layout_from_friend.setVisibility(View.GONE);
            viewHolder.layout_from_me.setVisibility(View.VISIBLE);

            /**
             * 先对消息进行处理，再设置本条item的消息内容和消息时间
             */
            SpannableStringBuilder sb=handler(viewHolder.msgContentFromMe,list.get(position).getMsgContent());
            viewHolder.msgContentFromMe.setText(sb);
            viewHolder.msgTime.setText(list.get(position).getMsgTime());

        }

        /**
         * 设置消息点击效果
         */
        viewHolder.msgContentFromFriend.setOnLongClickListener(new popAction(convertView,position, list.get(position).getFromOrTo()));
        viewHolder.msgContentFromMe.setOnLongClickListener(new popAction(convertView,position, list.get(position).getFromOrTo()));

        return convertView;
    }


    /**
     * 对消息进行处理，将文本中的表情解析出来，如果png有对应的gif,则显示gif，这里用到gif的处理工具类，这个工具类就不深入研究了
     * @param gifTextView
     * @param content
     * @return
     */
    private SpannableStringBuilder handler(final TextView gifTextView,String content) {
        SpannableStringBuilder sb = new SpannableStringBuilder(content);
        String regex = "(\\#\\[emoji/png/f_static_)\\d{3}(.png\\]\\#)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        while (m.find()) {
            String tempText = m.group();
            try {
                String num = tempText.substring("#[emoji/png/f_static_".length(), tempText.length()- ".png]#".length());
                String gif = "emoji/gif/f" + num + ".gif";
                /**
                 * 如果open这里不抛异常说明存在gif，则显示对应的gif
                 * 否则说明gif找不到，则显示png
                 * */
                InputStream is = context.getAssets().open(gif);
                sb.setSpan(new AnimatedImageSpan(new AnimatedGifDrawable(is,new AnimatedGifDrawable.UpdateListener() {
                            @Override
                            public void update() {
                                gifTextView.postInvalidate();
                            }
                        })), m.start(), m.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                is.close();
            } catch (Exception e) {
                String png = tempText.substring("#[".length(),tempText.length() - "]#".length());
                try {
                    sb.setSpan(new ImageSpan(context, BitmapFactory.decodeStream(context.getAssets().open(png))), m.start(), m.end(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
        return sb;
    }

    /**
     * 每个item中more按钮对应的点击动作
     */
    public class popAction implements View.OnLongClickListener {
        int position;
        View view;
        int fromOrTo;

        public popAction(View view, int position, int fromOrTo) {
            this.position = position;
            this.view = view;
            this.fromOrTo = fromOrTo;
        }

        @Override
        public boolean onLongClick(View v) {
            int[] arrayOfInt = new int[2];

            /**
             * 获取点击按钮的坐标
             */
            v.getLocationOnScreen(arrayOfInt);
            int x = arrayOfInt[0];
            int y = arrayOfInt[1];

            showPop(v, x, y, view, position, fromOrTo);
            return true;
        }
    }

    /**
     * 显示popWindow的方法
     * @param parent
     * @param x
     * @param y
     * @param view
     * @param position
     * @param fromOrTo
     */
    public void showPop(View parent, int x, int y, final View view,
                        final int position, final int fromOrTo) {
        /**
         * 设置popwindow显示位置
         */
        popupWindow.showAtLocation(parent, 0, x, y);

        /**
         * 获取popwindow焦点
         */
        popupWindow.setFocusable(true);

        /**
         * 设置popwindow如果点击外面区域，便关闭。
         */
        popupWindow.setOutsideTouchable(true);

        /**
         * 为copyTextView绑定点击复制监听事件
         */
        copyTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                /**
                 * 如果已经显示，再次点击取消popWindow
                 */
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }

                /**
                 * 获取剪贴板管理服务
                 */
                ClipboardManager cm = (ClipboardManager) context
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                /**
                 * 将文本数据复制到剪贴板,api 11+
                 */
                cm.setText(list.get(position).getMsgContent());
            }
        });

        /**
         * 为copyTextView绑定点击删除监听事件
         */
        deleteTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                if (fromOrTo == 0) {
                    /*接收到的消息，向左删除的动画*/

                    leftRemoveAnimation(view, position);
                } else if (fromOrTo == 1) {
                    /*发送的消息，向右删除的动画*/

                    rightRemoveAnimation(view, position);
                }

            }
        });

        /**
         * 更新popupWindow转态
         */
        popupWindow.update();
    }

    /**
     * 消息向右删除的动画
     * @param view
     * @param position
     */
    private void rightRemoveAnimation(final View view, final int position) {
        /**
         * 加载anim
         */
        final Animation animation = (Animation) AnimationUtils.loadAnimation(context, R.anim.chat_activity_listview_item_right_remove_anim);

        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                view.setAlpha(0);
                performDismiss(view, position);
                animation.cancel();
            }
        });

        view.startAnimation(animation);
    }

    /**
     * 消息向左删除的动画
     * @param view
     * @param position
     */
    private void leftRemoveAnimation(final View view, final int position) {
        /**
         * 加载anim
         */
        final Animation animation = (Animation) AnimationUtils.loadAnimation(context, R.anim.chat_activity_listview_item_left_remove_anim);

        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                view.setAlpha(0);
                performDismiss(view, position);
                animation.cancel();
            }
        });

        view.startAnimation(animation);
    }

    /**
     * 在此方法中执行item删除之后，其他的item向上或者向下滚动的动画，并且将position回调到方法onDismiss()中
     * @param dismissView
     * @param dismissPosition
     */
    @SuppressWarnings("all")
    private void performDismiss(final View dismissView,final int dismissPosition) {
        /**
         * 获取item的布局参数
         */
        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();

        /**
         * 获取item的高度
         */
        final int originalHeight = dismissView.getHeight();

        /**
         * 设置动画的duration
         */
        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0).setDuration(1000);

        /**
         * 开始动画
         */
        animator.start();

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

                list.remove(dismissPosition);
                notifyDataSetChanged();

                /**
                 * 这段代码很重要，因为我们并没有将item从ListView中移除，而是将item的高度设置为0
                 * 所以我们在动画执行完毕之后将item设置回来
                 */
                ViewHelper.setAlpha(dismissView, 1f);
                ViewHelper.setTranslationX(dismissView, 0);
                ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
                lp.height = originalHeight;
                dismissView.setLayoutParams(lp);
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                /**
                 * ListView删除某item之后，其他的item向上滑动
                 */
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                dismissView.setLayoutParams(lp);
            }
        });
    }
}