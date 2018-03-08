package com.denghb.log;

public interface Log {

    public void info(String format, Object... arguments);

    public void info(String msg, Throwable t);

    public void warn(String format, Object... arguments);

    public void warn(String msg, Throwable t);

    public void error(String format, Object... arguments);

    public void error(String msg, Throwable t);

    public boolean isDebugEnabled();

    public void debug(String format, Object... arguments);

    public void debug(String msg, Throwable t);

}
