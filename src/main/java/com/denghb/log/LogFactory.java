package com.denghb.log;

import com.denghb.utils.ConfigUtils;
import com.denghb.utils.ReflectUtils;

public class LogFactory {

    public static Log getLog(Class clazz) {

        String impl = ConfigUtils.getValue("log.impl", "com.denghb.log.impl.SimpleLogImpl");
        Class implClass = ReflectUtils.loadClass(impl);
        Object object = ReflectUtils.constructorInstance(implClass, Class.class, clazz);
        return (Log) object;
    }
}
