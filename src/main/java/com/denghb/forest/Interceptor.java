package com.denghb.forest;

import com.denghb.forest.server.Request;

public interface Interceptor {

    /**
     * 返回值 == null 表示放行
     *
     * @param request
     * @return
     */
    Object before(Request request);

    /**
     * 处理结果
     *
     * @param result
     */
    void after(Object result);
}
