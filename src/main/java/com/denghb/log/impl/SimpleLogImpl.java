package com.denghb.log.impl;

import com.denghb.log.Log;
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
        outLog("DEBUG", format, arguments);
    }

    public void debug(String msg, Throwable t) {
        outLog("DEBUG", msg, t);
    }

    public void error(String format, Object... arguments) {
        outLog("ERROR", format, arguments);
    }

    public void error(String msg, Throwable t) {
        outLog("ERROR", msg, t);
    }


    private void outLog(String level, String format, Object... arguments) {
        String log = DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS\t[") + Thread.currentThread().getName() + "]\t" + clazz.getName() + "\t[" + level + "]\t";
        log += format;

        for (Object object : arguments) {
            log = log.replaceFirst("\\{\\}", String.valueOf(object));
        }

        System.out.println(log);
    }

    private void outLog(String level, String msg, Throwable t) {

        String log = DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS\t[") + Thread.currentThread().getName() + "]\t" + clazz.getName() + "\t[" + level + "]\t";

        System.out.println(log + msg);
        t.printStackTrace();
    }
}
