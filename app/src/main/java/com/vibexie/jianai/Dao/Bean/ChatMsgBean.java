package com.vibexie.jianai.Dao.Bean;

/**
 * 聊天信息记录表，对应于id_chat_msg.db
 * Created by vibexie on 4/29/15.
 */
public class ChatMsgBean {
    /**
     * 消息id
     */
    private int _id;

    /**
     * 消息的接发时间
     */
    private String msgTime;

    /**
     * 消息内容
     */
    private String msgContent;

    /**
     * 接收还是发送,0接收，1发送
     */
    private int fromOrTo;

    /**
     * 消息是否已读,0未读，1已读
     */
    private int read;

    /**
     * getter and setter
     */
    public int get_Id() {
        return _id;
    }

    public void set_Id(int id) {
        this._id = id;
    }

    public String getMsgTime() {
        return msgTime;
    }

    public void setMsgTime(String msgTime) {
        this.msgTime = msgTime;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public int getFromOrTo() {
        return fromOrTo;
    }

    public void setFromOrTo(int fromOrTo) {
        this.fromOrTo = fromOrTo;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }
}