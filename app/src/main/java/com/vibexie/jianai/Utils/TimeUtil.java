package com.vibexie.jianai.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {

    /**
     * 获取当前标准时间 格式 年:月:日 时：分:秒
     * @return
     */
    public static String getCurrentStandardTime(){
        Date date=new Date();
        DateFormat dateFormat=new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        return dateFormat.format(date);
    }

    public static String millisTimeToStandardTime(long millisTime){
        Date date=new Date(millisTime);

        DateFormat dateFormat=new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        return dateFormat.format(date);
    }
}