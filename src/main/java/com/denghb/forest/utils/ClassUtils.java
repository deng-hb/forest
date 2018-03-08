package com.denghb.forest.utils;

import com.denghb.cache.Cache;
import com.denghb.cache.impl.SimpleCacheImpl;
import com.denghb.eorm.Eorm;
import com.denghb.log.Log;
import com.denghb.utils.ConfigUtils;
import com.denghb.utils.ReflectUtils;

import java.util.Arrays;

public class ClassUtils<T> {


    private final static Cache _CACHE = new SimpleCacheImpl();


    /**
     * 创建
     *
     * @param clazz
     * @param args
     * @param <T>
     * @return
     */
    public static <T> T create(Class<T> clazz, Object... args) {

        String key = clazz.getName() + Arrays.toString(args);

        Object object = _CACHE.get(key);
        if (null != object) {
            return (T) object;
        }

        if (clazz == Log.class) {
            String impl = ConfigUtils.getValue("log.impl", "com.denghb.log.impl.SimpleLogImpl");
            Class implClass = ReflectUtils.loadClass(impl);
            object = ReflectUtils.constructorInstance(implClass, Class.class, args[0]);
        } else if (clazz == Cache.class) {
            String impl = ConfigUtils.getValue("cache.impl", "com.denghb.cache.impl.SimpleCacheImpl");
            Class implClass = ReflectUtils.loadClass(impl);
            object = ReflectUtils.constructorInstance(implClass);
        }  else if (clazz == Eorm.class) {
            // 数据库实例化
            String impl = ConfigUtils.getValue("eorm.impl", "com.denghb.eorm.impl.EormMySQLImpl");
            String url = ConfigUtils.getValue("eorm.url");
            String username = ConfigUtils.getValue("eorm.username");
            String password = ConfigUtils.getValue("eorm.password");
            Class implClass = ReflectUtils.loadClass(impl);
            object = ReflectUtils.constructorInstance(implClass, new Class[]{String.class, String.class, String.class}, new Object[]{url, username, password});
        } else {
            int length = args.length;
            Class[] classes = new Class[length];
            for (int i = 0; i < args.length; i++) {
                Object obj = args[i];
                classes[i] = obj.getClass();
            }
            object = ReflectUtils.constructorInstance(clazz, classes, args);
        }

        _CACHE.set(key, object);
        return (T) object;
    }

}
