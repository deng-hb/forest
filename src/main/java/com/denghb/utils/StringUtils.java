package com.denghb.utils;

public class StringUtils {

    public static boolean isBlank(String str) {
        return null == str || 0 == str.trim().length();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
