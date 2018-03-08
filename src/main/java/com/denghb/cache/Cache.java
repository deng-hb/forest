package com.denghb.cache;

public interface Cache {

    /**
     * 获取对象
     *
     * @param key
     * @return
     */
    Object get(String key);

    /**
     * 移除对象
     *
     * @param key
     * @return
     */
    void remove(String key);

    /**
     * 设置缓存
     *
     * @param key
     * @param object
     */
    void set(String key, Object object);

    /**
     * 清理
     */
    void clear();
}
