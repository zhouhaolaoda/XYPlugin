package com.xylink.sdk.sample.utils;


/**
 * Created by chenshuliang on 2018/4/11.
 * teim
 */

public class CommonTime {

    public static String formatTime(long time) {
        String hh = time / 3600 > 9 ? time / 3600 + "" : "0" + time / 3600;
        String mm = (time % 3600) / 60 > 9 ? (time % 3600) / 60 + "" : "0" + (time % 3600) / 60;
        String ss = (time % 3600) % 60 > 9 ? (time % 3600) % 60 + "" : "0" + (time % 3600) % 60;
        return hh+":"+mm+":"+ss;
    }
}
