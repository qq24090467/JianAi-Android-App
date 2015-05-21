package com.vibexie.jianai.Chat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.vibexie.jianai.Constants.ServerConf;
import com.vibexie.jianai.Dao.Bean.ChatMsgBean;
import com.vibexie.jianai.Dao.DBHelper.UserDBHelper;
import com.vibexie.jianai.Dao.DBManager.DBManager;
import com.vibexie.jianai.MainActivity.MainActivity;
import com.vibexie.jianai.MiniPTR.MiniPTRFrame;
import com.vibexie.jianai.MiniPTR.MiniPTROnRefreshListener;
import com.vibexie.jianai.Services.XMPPservice.XMPPService;
import com.vibexie.jianai.Utils.ActivityStackManager;
import com.vibexie.jianai.R;
import com.vibexie.jianai.Utils.EditTextWithPrompt;
import com.vibexie.jianai.Utils.FreshDatasManager;
import com.vibexie.jianai.Utils.NetworkSettingsAlertDialog;
import com.vibexie.jianai.Utils.TimeUtil;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatActivity extends Activity implements View.OnClickListener,MiniPTROnRefreshListener{
    /**
     * 显示消息的listView
     */
    private ListView listView;

    /**
     * listView适配器
     */
    private ListViewAdapter listViewAdapter;

    /**
     * 点击显示emoji的按钮
     */
    private Button emojiButton;

    /**
     * 点击发送其他内容的按钮
     */
    private Button sendOthersButton;

    /**
     * 输入消息的editText
     */
    private EditTextWithPrompt inputMsgEditText;

    /**
     * 发送消息的按钮，这个按钮有发送语音和发送文本消息的功能
     */
    private Button sendMsgButton;

    /**
     * 容纳emoji的布局,注意包扩了ImageView和ViewPagerDots
     */
    private LinearLayout emojiContainer;

    /**
     * emojiViewPager，注意内部是GridView用于显示emoji
     */
    private ViewPager emojiViewPager;

    /**
     * 显示emoji ViewPager页数的点
     */
    private LinearLayout emojiViewPagerDotsContainer;

    /**
     * 表情文件名列表
     */
    private List<String> emojiFileNameList;

    /**
     * viewPager的pager list列表，每个pager里面都是一个gridview
     */
    private List<GridView> viewPagerList=new ArrayList<GridView>();

    /**
     * 每页的viewPager的表情是3行
     */
    private int emojiRows=3;

    /**
     * 每页的viewPager的表情是7列
     */
    private int emojiColumns=7;

    /**
     * 全局的DBHelper，在initMsgAndMsgListener方法中初始化
     */
    private UserDBHelper userDBHelper;

    /**
     * 全局的DBManager，在initMsgAndMsgListener方法中初始化，在onDestroy()方法中调用dbManager.close()关闭数据库
     */
    private DBManager dbManager;

    /**
     * 在sqliteDatabase中获取刷新数据的管理类，在initMsgAndMsgListener方法中初始化
     */
    private FreshDatasManager<ChatMsgBean> freshDatasManager;

    /**
     * 显示的消息
     */
    private LinkedList<ChatMsgBean> msgs=new LinkedList<>();

    /**
     * 下拉刷新框架
     */
    MiniPTRFrame chatMiniPTRFrame;

    /**
     * lover's name,从MainActivity中获取
     */
    private String loverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        /**
         * 获取lover用户名
         */
        loverName=getIntent().getStringExtra("lovername");

        /**
         * 压入到ActivityStack中
         */
        ActivityStackManager.getInstance().push(this);

        /**
         * 初始化所有控件
         */
        initAllWidgets();

        /**
         * 初始化消息和消息监听器
         */
        initMsgAndMsgListener();

        /**
         * 初始化UI的所有监听事件
         */
        initAllListeners();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /**
         * 从ActivityStack中弹出
         */
        ActivityStackManager.getInstance().pop(this);

        /**
         * 关闭数据库连接
         */
        dbManager.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            /**
             * 点击选择表情按钮事件
             */
            case R.id.choose_emoji_button :
                /**
                 * 隐藏软键盘
                 */
                hideSoftKeyboard();

                if(emojiContainer.getVisibility()==View.GONE){
                    emojiContainer.setVisibility(View.VISIBLE);
                }else {
                    emojiContainer.setVisibility(View.GONE);
                }
                break;

//            /**
//             * 点击发送其他按钮
//             */
//            case R.id.send_others_button:
//                break;

            /**
             * 点击输入框，进行输入
             */
            case R.id.input_msg_edittext:
                /**
                 * 如果表情选择布局打开，将其关闭
                 */
                if(emojiContainer.getVisibility()==View.VISIBLE){
                    emojiContainer.setVisibility(View.GONE);
                }

                break;

            /**
             * 点击发送消息按钮
             */
            case R.id.send_msg_button:

                String msgString=inputMsgEditText.getText().toString();

                if(!TextUtils.isEmpty(msgString)){

                    sendMsg(msgString);
                    inputMsgEditText.setText("");
                }

                break;
        }
    }

    /**
     * 初始化所有控件
     */
    private void initAllWidgets(){

        emojiButton=(Button)this.findViewById(R.id.choose_emoji_button);

        //sendOthersButton=(Button)this.findViewById(R.id.send_others_button);

        inputMsgEditText=(EditTextWithPrompt)this.findViewById(R.id.input_msg_edittext);

        sendMsgButton=(Button)this.findViewById(R.id.send_msg_button);

        emojiContainer=(LinearLayout)this.findViewById(R.id.activity_chat_emoji_container);

        emojiViewPager=(ViewPager)this.findViewById(R.id.emoji_viewpager);

        emojiViewPagerDotsContainer=(LinearLayout)this.findViewById(R.id.activity_chat_emoji_viewpager_dots_container);

        listView=(ListView)this.findViewById(R.id.msgListView);

        chatMiniPTRFrame=(MiniPTRFrame)this.findViewById(R.id.chat_miniptr_frame);

        /**
         * 调用initEmojiViewPager初始化EmojiViewPager
         */
        initEmojiViewPager();

        /**
         * 关闭软键盘
         */
        hideSoftKeyboard();
    }

    /**
     * 进入ChatActivity初始化消息记录和消息监听
     */
    private void initMsgAndMsgListener(){

        userDBHelper=new UserDBHelper(getApplicationContext(), MainActivity.USERID+".db");
        dbManager=new DBManager(userDBHelper);
        freshDatasManager=new FreshDatasManager<ChatMsgBean>(dbManager.getSqLiteDatabase(),new ChatMsgBean(),"friend_msg",20);

        /**
         * 获取表中的第一组数据，之后就下拉刷新,这里的判断是可能没有数据，这样会造成控指针异常
         */
        ArrayList<ChatMsgBean> tmpRefreshBeans;
        if((tmpRefreshBeans=freshDatasManager.getRefreshBeans())!=null){
            msgs.addAll(0, tmpRefreshBeans);
        }

        listViewAdapter = new ListViewAdapter(this, msgs);
        listView.setAdapter(listViewAdapter);
        listView.setSelection(msgs.size()-1);

        /**
         * 注册消息更新广播接收者
         */
        MsgUpdateReceiver msgUpdateReceiver=new MsgUpdateReceiver();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("com.vibexie.jianai.ChatModule.ChatActivity.MsgUpdate");
        ChatActivity.this.registerReceiver(msgUpdateReceiver,intentFilter);

    }

    /**
     * 初始化所有监听事件
     */
    private void initAllListeners(){
        /**
         * 所有参数为this的将在重载的public void onClick(View v)方法中统一处理
         */

        emojiButton.setOnClickListener(this);

//        sendOthersButton.setOnClickListener(this);

        inputMsgEditText.setOnClickListener(this);

        sendMsgButton.setOnClickListener(this);

        emojiViewPager.setOnPageChangeListener(new EmojiViewPagerListener());

        /**
         * 下拉刷新监听
         */
        chatMiniPTRFrame.setOnRefreshListener(this);
    }

    /**
     * 初始化EmojiViewPager
     */
    private void initEmojiViewPager(){

        /**
         * 1.初始化表情列表
         */
        emojiFileNameList=new ArrayList<String>();

        try {
            String[] emojis=getAssets().list("emoji/png");

            /**
             * 将Assets中的表情名称转为字符串一一添加进emojiFileNameList
             */
            for(int i=0;i<emojis.length;i++){
                emojiFileNameList.add(emojis[i]);
            }

            /**
             * 去掉删除图片
             */
            emojiFileNameList.remove("emotion_del_normal.png");

        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * 2.根据表情数量以及GridView设置的行数和列数计算Pager数量
         */
        int pageCount=( emojiFileNameList.size() % (emojiColumns * emojiRows -1) ==0
                ? emojiFileNameList.size() / (emojiColumns * emojiRows -1)
                : emojiFileNameList.size() / (emojiColumns * emojiRows -1) + 1 );

        /**
         * 3.初始化每页的gridView,以及当表情页改变时，dots效果也要跟着改变
         */
        for(int i=0;i<pageCount;i++){
            viewPagerList.add(viewPagerItem(i));
            ViewGroup.LayoutParams layoutParams=new ViewGroup.LayoutParams(16,16);
            emojiViewPagerDotsContainer.addView(viewPagerDotsItem(i),layoutParams);
        }

        EmojiViewPagerAdapter emojiViewPagerAdapter=new EmojiViewPagerAdapter(viewPagerList);
        emojiViewPager.setAdapter(emojiViewPagerAdapter);
        emojiViewPagerDotsContainer.getChildAt(0).setSelected(true);

    }

    /**
     * 获取viewpager position的一页或者说是一项，也就是一个gridView
     * @param position
     * @return
     */
    private GridView viewPagerItem(int position) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.activity_chat_emoji_viewpager_gridview, null);//表情布局

        /**
         * 将返回的gridview
         */
        GridView gridview = (GridView) layout.findViewById(R.id.chat_emoji_gridview);

        /**
         * 因为每一页末尾都有一个删除图标，所以每一页的实际表情columns * rows － 1; 空出最后一个位置给删除图标
         * subList是第position页的表情列表
         */
        List<String> subList = new ArrayList<String>();
        subList.addAll(emojiFileNameList.subList(position * (emojiColumns * emojiRows - 1),
                                            (emojiColumns * emojiRows - 1) * (position + 1) > emojiFileNameList.size()
                                            ? emojiFileNameList.size()
                                            : (emojiColumns* emojiRows - 1) * (position + 1)));
        /**
         * 末尾添加删除图标
         */
        subList.add("emotion_del_normal.png");

        /**
         * 通过适配器加载真正的表情图片
         */
        EmojiGridViewAdapter emojiGridViewAdapter=new EmojiGridViewAdapter(subList,this);
        gridview.setAdapter(emojiGridViewAdapter);
        gridview.setNumColumns(emojiColumns);

        /**
         * 单击表情执行的操作
         */
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                try {
                    /**
                     * 这里注意，在EmojiGridViewAdapter对emoji进行适配的时候就将对应的text设置为了emoji文件路径
                     */
                    String emojiPath = ((TextView)((LinearLayout) view).getChildAt(1)).getText().toString();

                    if (!emojiPath.contains("emotion_del_normal")) {//
                        /**点击的不是删除emoji,将选中的emoji加入到输入框中*/

                        insertEmojiToCursor(getEmoji(emojiPath));
                    } else {
                        /**点击的是删除emoji，将当前光标的前一个emoji或者字符删除**/

                        deleteFrontOfCursor();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return gridview;
    }

    /**
     * 获取viewPagerDots position的一项，也就是一个ImageView，一点
     * @param position
     */
    private ImageView viewPagerDotsItem(int position){
        LayoutInflater inflater=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        ImageView imageView=(ImageView)inflater.inflate(R.layout.activity_chat_emoji_viewpager_dots_image,null).findViewById(R.id.emoji_viewpage_dots);
        imageView.setId(position);

        return imageView;
    }

    /**
     * 监听EmojiViewPager的滑动事件，这里主要是监听到改变后，dots也要相应的改变
     */
    private class EmojiViewPagerListener implements ViewPager.OnPageChangeListener{

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            /**
             * 将所有的dots设为不选中
             */
            for(int i=0;i<emojiViewPagerDotsContainer.getChildCount();i++){
                emojiViewPagerDotsContainer.getChildAt(i).setSelected(false);
            }

            /**
             * 将当前的dot设为选中
             */
            emojiViewPagerDotsContainer.getChildAt(position).setSelected(true);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    /**
     * SpannableStringBuilder和SpannableString用来修改文本的样式。
     * 这里的作用也就是将emoji路径转换为emoji图片在文本中显示
     * @param emojiPath
     * @return  SpannableStringBuilder
     */
    private SpannableStringBuilder getEmoji(String emojiPath){
        SpannableStringBuilder builder=new SpannableStringBuilder();

        String tmp="#["+emojiPath+"]#";
        builder.append(tmp);
        try {
            builder.setSpan(new ImageSpan(ChatActivity.this,
                                          BitmapFactory.decodeStream(getAssets().open(emojiPath))),
                                          builder.length()-tmp.length(),/*也就是0*/
                                          builder.length(),
                                          Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder;
    }

    /**
     * 插入一个表情到消息输入框当前光标处
     * @param text
     */
    private void insertEmojiToCursor(CharSequence text){
        /**
         * 获取当前光标的位置
         */
        int startCursor= Selection.getSelectionStart(inputMsgEditText.getText());

        /**
         * 获取光标的边界
         */
        int endCursor=Selection.getSelectionEnd(inputMsgEditText.getText());

        if(startCursor!=endCursor){
            ((Editable)inputMsgEditText.getText()).replace(startCursor,endCursor,"");
        }

        endCursor=Selection.getSelectionEnd(inputMsgEditText.getText());
        ((Editable)inputMsgEditText.getText()).insert(endCursor,text);
    }

    /**
     * 从消息输入框光标前删除一个emoji或者一个字符，需要判断
     */
    private void deleteFrontOfCursor(){
        if(inputMsgEditText.getText().length()!=0){
            /*输入框中不为空才有继续的必要*/

            int startCursor=Selection.getSelectionStart(inputMsgEditText.getText());
            int endCursor=Selection.getSelectionEnd(inputMsgEditText.getText());

            if(endCursor>0){
                if (endCursor == startCursor) {
                    if (isFrontAEmoji(endCursor)) {
                        String st = "#[emoji/png/f_static_000.png]#";
                        ((Editable) inputMsgEditText.getText()).delete(endCursor - st.length(), endCursor);
                    } else {
                        ((Editable) inputMsgEditText.getText()).delete(endCursor - 1,endCursor);
                    }
                } else {
                    ((Editable) inputMsgEditText.getText()).delete(startCursor,endCursor);
                }
            }
        }
    }

    /**
     * 判断即将当前光标前是emoji还是字符
     * @param cursor
     * @return
     */
    private boolean isFrontAEmoji(int cursor){
        /**
         * 参考字符串，当然也可以用这个字符串的长度作参考
         */
        String st = "#[emoji/png/f_static_000.png]#";

        /**
         * 获取起始位置到光标处之间的字符串
         */
        String content = inputMsgEditText.getText().toString().substring(0, cursor);

        if (content.length() >= st.length()) {
            /*如果content的长度大于等于参考字符串的长度，才可能是emoji，否则必定是普通字符串*/

            /**
             * 假设是emoji,截取光标前的st.length长度字符串
             */
            String checkStr = content.substring(content.length() - st.length(),
                    content.length());

            /**
             * 用正则表达式进行匹配
             */
            String regex = "(\\#\\[emoji/png/f_static_)\\d{3}(.png\\]\\#)";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(checkStr);

            /**
             * 是假设正确，返回true，假设错误则返回false
             */
            return m.matches();
        }

        return false;
    }

    /**
     * 关闭软键盘的方法
     */
    private void hideSoftKeyboard(){
        InputMethodManager inputMethodManager=(InputMethodManager)this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 将自己发出的消息转化为bean
     * @param msg
     * @return
     */
    private ChatMsgBean getChatMsgBeanFromMe(String msg){
        ChatMsgBean chatMsgBean=new ChatMsgBean();

        chatMsgBean.setMsgContent(msg);
        chatMsgBean.setFromOrTo(1);
        chatMsgBean.setMsgTime(TimeUtil.getCurrentStandardTime());

        return chatMsgBean;
    }

    /**
     * 消息更新的广播接收者，MainActivity的消息分发方法发送广播通知本activity已经接收消息，立即从数据库读取一条消息
     * 注意广播中携带了新接收消息的_id
     */
    private class MsgUpdateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            /**
             * 获取广播中携带的id
             */
            int _id=intent.getIntExtra("_id",-1);
            if(_id>-1){
                ArrayList<ChatMsgBean> lists=new ArrayList<ChatMsgBean>();

                /**
                 *读取数据库中的该消息
                 */
                lists=(dbManager.getBeans(new ChatMsgBean(),"friend_msg","_id=?",new String[]{String.valueOf(_id)}));

                /**
                 * 更新显示消息
                 */
                msgs.add(lists.get(lists.size()-1));
                listViewAdapter.resetList(msgs);
                listViewAdapter.notifyDataSetChanged();
                listView.setSelection(msgs.size()-1);
            }
        }
    }

    /**
     * 保存消息并发送
     * @param msg
     */
    private void sendMsg(String msg){

        /**
         * 从XMPPService中获取chatManager对象，注意这个值在XMPPService中每一次重连服务器，都会改变
         * 所以这里不用考虑chatManager是否是一个正确连接的的ChatManager
         */
        ChatManager chatManager= XMPPService.chatManager;

        if(chatManager==null){
            /*未登录成功，在XMPPService中可能未初始化，这里几乎都是网络未连接导致的*/

            NetworkSettingsAlertDialog networkSettingsAlertDialog=new NetworkSettingsAlertDialog(this);
            networkSettingsAlertDialog.show();

            /**
             * 直接返回
             */
            return;
        }

        /**
         * 构建bean
         */
        ChatMsgBean chatMsgBean=new ChatMsgBean();
        chatMsgBean.setMsgTime(TimeUtil.getCurrentStandardTime());
        chatMsgBean.setMsgContent(msg);
        chatMsgBean.setFromOrTo(1);
        chatMsgBean.setRead(0);

        Chat chat=chatManager.createChat(loverName+"@"+ ServerConf.OPENFIRE_SERVER_HOSTNAME, null);
        if(dbManager.insertBean("friend_msg",chatMsgBean)){
            /*插入数据库成功才发送消息给对方*/

            try {
                chat.sendMessage(msg);
            } catch (XMPPException e) {
                e.printStackTrace();
            }

            /**
             *更新listView的消息显示
             */
            msgs.add(getChatMsgBeanFromMe(msg));
            listViewAdapter.resetList(msgs);
            listViewAdapter.notifyDataSetChanged();
            listView.setSelection(msgs.size()-1);

        }else {
            /*插入数据库失败，暂且认为是网络原因造成的*/

            NetworkSettingsAlertDialog networkSettingsAlertDialog=new NetworkSettingsAlertDialog(this);
            networkSettingsAlertDialog.show();
        }
    }

    /**
     * 下拉刷新操作
     */
    @Override
    public void onRefresh() {
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                /**
                 * 更新显示消息
                 */
                msgs.addAll(0, freshDatasManager.getRefreshBeans());
                listViewAdapter.resetList(msgs);

                //listViewAdapter.notifyDataSetChanged();
                listView.setSelection(19);
            }
        };

        new Thread(){
            @Override
            public void run() {
                super.run();


                /**
                 * 显示正在刷新1秒
                 */
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                chatMiniPTRFrame.refreshSuccess();

                /**
                 * 显示刷新结果0.5秒
                 */
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                handler.sendEmptyMessage(0);

            }
        }.start();
    }
}