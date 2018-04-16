package com.denghb.forest.utils;

import java.util.Map;

public class PathCompareUtils {


    /**
     * 正则搞不懂，硬解析
     *
     * @param path1 /x/ss{id}
     * @param path2 /x/ss234
     *              {id=234}
     */
    public static void buildPath(String path1, String path2, Map<String, String> pathVar) {
        int start = path1.indexOf('{');
        if (-1 == start) {
            return;
        }

        String tmp1 = path1.substring(0, start);
        if (!path2.startsWith(tmp1)) {
            return;// 不属于
        }
        String[] tmp1s = path1.substring(start, path1.length()).split("\\/");
        String[] tmp2s = path2.substring(start, path2.length()).split("\\/");
        if (tmp1s.length != tmp2s.length || 0 == tmp1s.length) {
            return;
        }

        // 假定他们是一样的
        for (int i = 0; i < tmp1s.length; i++) {
            String key = tmp1s[i];
            String value = tmp2s[i];

            int start1 = key.indexOf('{');
            int end1 = key.indexOf('}');

            if (0 != start1 || end1 != key.length() - 1) {
                // 需要掐头去尾
                if (start1 > 0) {
                    String startStr = key.substring(0, start1);
                    if (!value.startsWith(startStr)) {
                        pathVar.clear();
                        return;// 不匹配
                    }
                    value = value.substring(start1, value.length());
                }

                // 去尾
                if (end1 != key.length() - 1) {
                    String endKeyStr = key.substring(end1 + 1, key.length());
                    if (!value.endsWith(endKeyStr)) {
                        pathVar.clear();
                        return;// 不匹配
                    }
                    value = value.substring(0, value.indexOf(endKeyStr));
                }
            }

            key = key.substring(start1 + 1, end1);
            pathVar.put(key, value);

        }

    }

    /**
     * /* -> /x/xx || /xxx/xx/xx..  true
     * /*a/aa -> /xxxa/aa  true
     */
    public static boolean comparePath(String origin, String uri) {

        String[] tmp1s = origin.split("\\/");
        String[] tmp2s = uri.split("\\/");

        int len1 = tmp1s.length;
        int len2 = tmp2s.length;

        for (int i = 0; i < len1; i++) {
            String s1 = tmp1s[i];

            if ("*".equals(s1)) {
                return true;
            }
            String s2 = tmp2s[i];

            int start = s1.indexOf('*');
            if (-1 < start) {

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
