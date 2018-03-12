package com.denghb.log;

import com.denghb.forest.utils.ClassUtils;

public class LogFactory {

    public static Log getLog(Class clazz) {
        return ClassUtils.create(Log.class, clazz);
    }
}
