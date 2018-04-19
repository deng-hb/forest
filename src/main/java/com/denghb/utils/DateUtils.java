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

    /**
     * 能预测大多数日期字符串
     */
    public static Date parse(String source) {
        if (null == source) {
            return null;
        }
        source = source.trim();

        String pattern;

        if (StringUtils.isNumeric(source)) {
            return new Date(Long.parseLong(source));
        }
        if (source.indexOf(" ") == 3) {
            pattern = "EEE MMM dd HH:mm:ss zzz yyyy";
        } else if (source.contains("GMT")) {
            pattern = "d MMM yyyy HH:mm:ss 'GMT'";
        } else {
            //
            pattern = source;
            pattern = pattern.replaceFirst("^[0-9]{4}([^0-9])", "yyyy$1");
            pattern = pattern.replaceFirst("^[0-9]{2}([^0-9])", "yy$1");
            pattern = pattern.replaceFirst("([^0-9])[0-9]{1,2}([^0-9])", "$1MM$2");
            pattern = pattern.replaceFirst("([^0-9])[0-9]{1,2}( ?)", "$1dd$2");
            pattern = pattern.replaceFirst("( )[0-9]{1,2}([^0-9])", "$1HH$2");
            pattern = pattern.replaceFirst("([^0-9])[0-9]{1,2}([^0-9]?)", "$1mm$2");
            pattern = pattern.replaceFirst("([^0-9])[0-9]{1,2}([^0-9]?)", "$1ss$2");
            pattern = pattern.replaceFirst("([^0-9])[0-9]{1,3}([^0-9]?)", "$1S");
        }
        return parse(source, pattern);
    }


    public static void main(String[] args) {

        String str = new Date().toString();
        System.out.println("default:" + str);
        System.out.println("default parse:" + (null != parse(str)));

        String gtm = new Date().toGMTString();
        System.out.println("GTM:" + gtm);
        System.out.println("GTM parse:" + (null != parse(gtm)));


        String locale = new Date().toLocaleString();
        System.out.println("locale:" + locale);
        System.out.println("locale parse:" + (null != parse(locale)));

        String custom = "2017/12/23 18:23:33.234";
        System.out.println("custom:" + custom);
        System.out.println("locale parse:" + (null != parse(custom)));

        String other = "2017年12月23日 18时23分";
        System.out.println("other:" + other);
        System.out.println("other parse:" + (null != parse(other)));


    }
}
