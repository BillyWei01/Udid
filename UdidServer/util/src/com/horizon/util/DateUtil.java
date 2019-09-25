package com.horizon.util;

import java.text.SimpleDateFormat;

public class DateUtil {
    private static final ThreadLocal<SimpleDateFormat> sDateFormatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    public static String now(){
       return sDateFormatter.get().format(System.currentTimeMillis());
    }
}

