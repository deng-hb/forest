package com.denghb.forest;

import com.denghb.cache.Cache;
import com.denghb.cache.impl.SimpleCacheImpl;

public class Context<T> {


    private final static Cache _CACHE = new SimpleCacheImpl();

    public static <T> T getBean(Class<T> clazz) {

        String key = clazz.getName();
        return (T) _CACHE.get(key);
    }

    public static <T> void setBean(Class<T> clazz, T object) {
        String key = clazz.getName();
        _CACHE.set(key, object);
    }

}
