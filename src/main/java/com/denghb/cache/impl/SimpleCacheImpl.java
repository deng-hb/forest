package com.denghb.cache.impl;

import com.denghb.cache.Cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleCacheImpl implements Cache {

    private static Map<String, Object> DATA = new ConcurrentHashMap<String, Object>();

    public Object get(String key) {
        return DATA.get(key);
    }

    public void remove(String key) {
        DATA.remove(key);
    }

    public void set(String key, Object object) {
        DATA.put(key, object);
    }

    public void clear() {
        DATA.clear();
    }
}
