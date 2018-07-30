package com.denghb.utils;

import java.math.BigDecimal;

public class StringUtils {

    public static boolean isBlank(String str) {
        return null == str || 0 == str.trim().length();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean isNumeric(String str) {
        try {
            new BigDecimal(str);
            return true;
        } catch (Exception e) {
            return false;//异常 说明包含非数字。
        }
    }

    public static void main(String[] args) {

        System.out.println(isNumeric("123"));
    }
}
