package com.vibexie.jianai.Chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vibexie.jianai.R;

import java.io.IOException;
import java.util.List;

/**
 * ViewPager中的一页，是一个GridView,此类就是gridView的适配器
 * Created by vibexie on 4/27/15.
 */
public class EmojiGridViewAdapter extends BaseAdapter{
    /**
     * 本页表情的文件名list
     */
    private List<String> list;

    /**
     * 上下文
     */
    private Context context;

    /**
     * 构造方法
     * @param list
     * @param context
     */
    public EmojiGridViewAdapter(List<String> list,Context context){
        this.list=list;
        this.context=context;
    }

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHodler viewHodler;

        if(convertView==null){
            viewHodler=new ViewHodler();
            convertView=LayoutInflater.from(context).inflate(R.layout.activity_chat_emoji_image,null);
            viewHodler.imageView=(ImageView)convertView.findViewById(R.id.emoji_image);
            viewHodler.textView=(TextView)convertView.findViewById(R.id.emoji_text);
            convertView.setTag(viewHodler);

        }else {
            viewHodler=(ViewHodler)convertView.getTag();
        }

        /**
         * 从assets目录读取表情图片，并进行适配
         */
        try {
            Bitmap bitmap= BitmapFactory.decodeStream(context.getAssets().open("emoji/png/"+list.get(position)));
            viewHodler.imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * 这一句的作用是，在加载emoji时，设置textView的text为emoji路径，在选择emoji的时候可以通过text来定位是哪个emoji
         * 这也是为恶什么使用一个ViewHolder的原因
         */
        viewHodler.textView.setText("emoji/png/"+list.get(position));
        return convertView;
    }

    /**
     * gridView的每个单元，也就是一张表情图片加表情文本，当然文本是不显示的，只是为了便于标识表情
     */
    class ViewHodler{
        ImageView imageView;
        TextView textView;
    }
}