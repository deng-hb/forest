package com.denghb.utils;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by denghb on 2016/12/16.
 */
public class ConfigUtils {


    // 配置文件名称
    private static String CONFIG_FILE = "forest.properties";

    private static Properties properties = new Properties();

    public static void init(String configPath) {
        if (null == configPath) {
            configPath = CONFIG_FILE;
        }
        InputStream in = null;
        try {
            in = new FileInputStream(configPath);
        } catch (Exception e) {

        }
        try {
            if (null == in) {
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream(configPath);
            }
            if (null != in) {
                properties.load(in);
                return;
            }
        } catch (Exception e) {

        } finally {
            close(in);
        }
    }

    private static void close(InputStream in) {
        if (null != in) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ConfigUtils() {
    }


    /**
     * 获取属性
     *
     * @param name
     * @return
     */
    public static String getValue(String name) {
        return getValue(name, null);
    }

    public static String getValue(String name, String defaultValue) {

        StringBuilder sb = new StringBuilder();

        String value = properties.getProperty(name);

        buildValue(sb, value);

        return sb.length() == 0 ? defaultValue : sb.toString();
    }

    private static void buildValue(StringBuilder sb, String value) {

        if (null == value) {
            return;
        }

        int start = value.indexOf("${");
        int end = value.indexOf("}");

        if (start != -1 && start < end) {
            // 前半截
            sb.append(value.substring(0, start));

            String name = value.substring(start + 2, end);

            // 翻译属性
            buildValue(sb, properties.getProperty(name));

            value = value.substring(end + 1, value.length());
            // 后半截
            buildValue(sb, value);

        } else {
            sb.append(value);
        }
    }


    public static void main(String... args) {
        System.out.println(getValue("com.denghb.slf4j2elk.level"));
    }

}
