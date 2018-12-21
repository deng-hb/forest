package com.denghb.forest.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RulePathMatcher {


    public static void main(String[] args) {
        List<String> urls = new ArrayList<String>();
//        urls.add("/,/,1");
//        urls.add("/*,/,1");
//        urls.add("/,/a,0");
//        urls.add("/a/*,/a/,1");
//        urls.add("/**,/ad/asd/s,1");
//        urls.add("/a/a,/a/a,1");
//        urls.add("/a/a,/a/b,0");
//        urls.add("/a/a,/a/,0");
        urls.add("/a/a**,/a/a,1");
        urls.add("/a/a*,/a/a,1");
        urls.add("/**.jpgc,/aba.jpg,0");
        urls.add("/**.jpg,/a/a.jpg,1");
        urls.add("/{a},/a,1");

        int i = 0;
        Map<String, String> var = new HashMap<String, String>();
        for (String url : urls) {
            i++;
            var.clear();
            String[] ss = url.split(",");
            boolean b = false;

            try {
                b = match(ss[0], ss[1], var);
            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean pre = b == "1".equals(ss[2]);

            String output = i + "=>" + url + ":" + pre;

            if (!var.isEmpty()) {
                output += "=>" + var;
            }

            if (pre) {
                System.out.println(output);
            } else {
                System.err.println(output);
            }
        }

    }

    /**
     * *  匹配一个以上的字符   /*     == (/ab || /cd)          != /a/b
     * ** 匹配一个以上的目录   /a/**  == (/a/cc || /a/wew/hug) != /b/xx
     * {} 匹配一个参数值      /{a}/c == (/abc/c => a->abc)    != /abc/d
     *
     * @param rule
     * @param path
     * @param pathVar
     * @return 是否匹配
     */
    public static boolean match(String rule, String path, Map<String, String> pathVar) {
        if (null == rule || null == path || !rule.startsWith("/") || !path.startsWith("/")) {
            return false;
        }

        if ("/".equals(rule)) {
            return rule.equals(path);
        }

        String[] ruleArr = rule.split("\\/");
        String[] pathArr = path.split("\\/");

        int ruleArrLen = ruleArr.length;
        int pathArrLen = pathArr.length;

        // 第一个是空不比较
        for (int i = 1; i < ruleArrLen; i++) {
            String s1 = ruleArr[i];

            if ("**".equals(s1) && ruleArrLen >= pathArrLen) {
                return true;
            }
            if ("*".equals(s1) && ruleArrLen > pathArrLen) {
                return true;
            }
            if (i >= pathArrLen) {
                return false;
            }
            String s2 = pathArr[i];

            int start2 = s1.indexOf("**");
            int start = s1.indexOf('*');
            if (-1 < start2) {

            } else if (-1 < start) {

                String s1start = s1.substring(0, start);

                if (!s2.startsWith(s1start)) {
                    return false;
                }
                int end = s1.lastIndexOf('*');
                String s1end = s1.substring(end + 1, s1.length());

                if (!s2.endsWith(s1end)) {
                    return false;
                }

            } else if (!s1.equals(s2)) {
                return false;
            }
        }

        return true;
    }


}
