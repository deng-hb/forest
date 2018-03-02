package com.denghb.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static String format(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        String str = sdf.format(date);
        return str;
    }

    public static Date parse(String source, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        Date date = null;
        try {
            date = sdf.parse(source);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
