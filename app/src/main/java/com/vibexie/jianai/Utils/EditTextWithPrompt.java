package com.vibexie.jianai.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 当edittext中发生cut,copy,paste事件时会给出Toast提示
 * Created by vibexie on 4/27/15.
 */
public class EditTextWithPrompt extends EditText implements MenuItem.OnMenuItemClickListener{
    private Context context;

    public EditTextWithPrompt(Context context) {
        super(context);
        this.context=context;
    }

    public EditTextWithPrompt(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
    }

    public EditTextWithPrompt(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return onTextContextMenuItem(item.getItemId());
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        switch (id){
            case android.R.id.cut:
                Toast.makeText(context,"文本已剪切",Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.paste:
                Toast.makeText(context,"文本已粘贴",Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.copy:
                Toast.makeText(context,"文本已复制",Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onTextContextMenuItem(id);
    }
}