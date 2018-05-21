package com.denghb.log.impl;

import com.denghb.log.Log;
import com.denghb.utils.ConfigUtils;
import com.denghb.utils.DateUtils;

import java.util.Date;

/**
 * 日志工具类
 */
public class SimpleLogImpl implements Log {

    private Class clazz;

    public SimpleLogImpl(Class clazz) {
        this.clazz = clazz;
    }

    public void info(String format, Object... arguments) {
        outLog("INFO", format, arguments);
    }

    public void info(String msg, Throwable t) {
        outLog("INFO", msg, t);
    }

    public void warn(String format, Object... arguments) {
        outLog("WARN", format, arguments);
    }

    public void warn(String msg, Throwable t) {
        outLog("WARN", msg, t);
    }

    public boolean isDebugEnabled() {
        return true;
    }

    public void debug(String format, Object... arguments) {
        boolean debug = "true".equals(ConfigUtils.getValue("debug"));
        if (debug)
            outLog("DEBUG", format, arguments);
    }

    public void debug(String msg, Throwable t) {
        boolean debug = "true".equals(ConfigUtils.getValue("debug"));
        if (debug)
            outLog("DEBUG", msg, t);
    }

    public void error(String format, Object... arguments) {
        outLog("ERROR", format, arguments);
    }

    public void error(String msg, Throwable t) {
        outLog("ERROR", msg, t);
    }


    private void outLog(String level, String format, Object... arguments) {
        StringBuilder sb = new StringBuilder();
        sb.append(DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS\t["));
        sb.append(Thread.currentThread().getName());
        sb.append("]\t");
        sb.append(clazz.getName());
        sb.append("\t[");
        sb.append(level);
        sb.append("]\t");

        for (Object object : arguments) {

            if (object instanceof Object[]) {
                Object[] arr = (Object[]) object;
                if (arr.length > 0) {
                    StringBuilder arg = new StringBuilder("[");
                    for (int i = 0; i < arr.length; i++) {
                        if (i != 0) {
                            arg.append(",");
                        }
                        Object obj = arr[i];
                        if (obj instanceof Date) {
                            arg.append(DateUtils.format((Date) obj, "yyyy-MM-dd HH:mm:ss.SSS"));
                        } else {
                            arg.append(obj);
                        }
                    }
                    arg.append("]");
                    format = format.replaceFirst("\\{\\}", arg.toString());
                } else {
                    format = format.replaceFirst("\\{\\}", "");
                }
            } else {
                format = format.replaceFirst("\\{\\}", String.valueOf(object));
            }

        }
        sb.append(format);

        System.out.println(sb.toString());
    }

    private void outLog(String level, String msg, Throwable t) {

        String log = DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS\t[") + Thread.currentThread().getName() + "]\t" + clazz.getName() + "\t[" + level + "]\t";

        System.out.println(log + msg);
        t.printStackTrace();
    }
}
