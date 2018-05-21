package com.denghb.forest;

import com.denghb.forest.annotation.PathVariable;
import com.denghb.forest.annotation.RequestBody;
import com.denghb.forest.annotation.RequestHeader;
import com.denghb.forest.annotation.RequestParameter;
import com.denghb.forest.model.ForestModel;
import com.denghb.forest.model.MethodModel;
import com.denghb.forest.model.ParameterModel;
import com.denghb.forest.model.RestModel;
import com.denghb.forest.server.Request;
import com.denghb.forest.server.Response;
import com.denghb.forest.server.ServerHandler;
import com.denghb.forest.utils.BeanFactory;
import com.denghb.forest.utils.PathCompareUtils;
import com.denghb.json.JSON;
import com.denghb.log.Log;
import com.denghb.utils.DateUtils;
import com.denghb.utils.ReflectUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForestHandler implements ServerHandler {

    private Log log;

    private boolean debug;

    public ForestHandler(Log log, boolean debug) {
        this.log = log;
        this.debug = debug;
    }

    public Response execute(Request request) {

        String uri = request.getUri();

        // TODO 运行状态
        if (debug && uri.equals("/forest")) {

            ForestModel forest = new ForestModel();

            List<RestModel> list = new ArrayList<RestModel>();
            forest.setRest(list);

            RestModel rest;
            for (String path : Forest._RESTful_Method.keySet()) {
                String method = path.substring(0, path.indexOf("/"));
                path = path.substring(path.indexOf("/"));
                rest = new RestModel();
                rest.setPath(path);
                rest.setMethod(method);
                list.add(rest);
            }
            return Response.build(forest);
        }

        log.info("{}\t{}\t{}", request.getHostAddress(), request.getMethod(), uri);

        Object object = handlerFilter(request);
        if (null != object) {
            return Response.build(object);
        }

        String path = request.getMethod() + uri;
        MethodModel methodModel = Forest._RESTful_Method.get(path);
        Map<String, String> pathVariables = new HashMap<String, String>();

        if (null == methodModel) {
            // 参数在path上的匹配
            for (String path1 : Forest._RESTful_Method.keySet()) {
                PathCompareUtils.buildPath(path1, path, pathVariables);
                if (!pathVariables.isEmpty()) {
                    methodModel = Forest._RESTful_Method.get(path1);
                    break;
                }
            }

            // 文件匹配
            URL url = this.getClass().getResource("/static" + uri);
            if (null == url && uri.equals("/favicon.ico")) {
                // 默认图标
                url = this.getClass().getResource("/static/forest.ico");
            }
            if (null != url) {
                File file = new File(url.getFile());
                if (file.exists() && !file.isDirectory()) {
                    return Response.build(file);
                }
            }

            if (pathVariables.isEmpty()) {
                Object result = handlerError(new ForestException("404 Not Found[" + path + "]", 404));
                if (null != result) {
                    return Response.build(result);
                }
                return Response.buildError(404);
            }
        }

        try {

            Object target = BeanFactory.getBean(methodModel.getClazz());

            // 执行path对应方法
            Method method = methodModel.getMethod();
            method.setAccessible(true);

            Object result = handlerBefore(request, pathVariables);
            if (null == result) {
                result = method.invoke(target, buildParams(methodModel, request, pathVariables));
            }
            result = handlerAfter(request, result, pathVariables);

            return Response.build(result);
        } catch (InvocationTargetException e) {
            // 调用方法抛出异常
            Object result = handlerError(e.getTargetException());
            if (null != result) {
                return Response.build(result);
            }
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            // 内部错误
            Object result = handlerError(new ForestException(e.getMessage(), 500));
            if (null != result) {
                return Response.build(result);
            }
        }

        return Response.buildError(500);
    }

    private Object handlerBefore(Request request, Map<String, String> pathVariables) {

        try {
            String key = request.getMethod() + request.getUri();

            MethodModel methodModel = null;
            for (String key1 : Forest._Before_Method.keySet()) {
                boolean b = PathCompareUtils.comparePath(key1, key);
                if (b) {
                    methodModel = Forest._Before_Method.get(key1);
                    break;
                }

            }
            if (null == methodModel) {
                return null;
            }

            Object target = BeanFactory.getBean(methodModel.getClazz());
            Object[] ps = buildParams(methodModel, request, pathVariables);

            Method method = methodModel.getMethod();
            method.setAccessible(true);
            return method.invoke(target, ps);
        } catch (InvocationTargetException e) {
            return handlerError(e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private Object handlerAfter(Request request, Object result, Map<String, String> pathVariables) {
        try {
            String key = request.getMethod() + request.getUri();

            MethodModel methodModel = null;
            for (String temp : Forest._After_Method.keySet()) {
                boolean b = PathCompareUtils.comparePath(temp, key);
                if (b) {
                    methodModel = Forest._After_Method.get(temp);
                    break;
                }

            }
            if (null == methodModel) {
                return result;
            }

            Object target = BeanFactory.getBean(methodModel.getClazz());


            Object[] ps = buildParams(methodModel, request, pathVariables);

            // TODO 返回值赋值
            for (int i = 0; i < ps.length; i++) {
                if (null == ps[i]) {
                    ps[i] = result;
                }
            }

            for (ParameterModel model : methodModel.getParameters()) {
                if (model.getType().equals(Object.class)) {

                }
            }

            Method method = methodModel.getMethod();
            method.setAccessible(true);
            return method.invoke(target, ps);
        } catch (InvocationTargetException e) {
            return handlerError(e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private Object handlerFilter(Request request) {
        try {
            String key = request.getMethod() + request.getUri();

            MethodModel methodModel = null;
            for (String key1 : Forest._Filter_Method.keySet()) {
                boolean b = PathCompareUtils.comparePath(key1, key);
                if (b) {
                    methodModel = Forest._Filter_Method.get(key1);
                    break;
                }

            }
            if (null == methodModel) {
                return null;
            }

            Object target = BeanFactory.getBean(methodModel.getClazz());
            Object[] ps = buildParams(methodModel, request, null);

            Method method = methodModel.getMethod();
            method.setAccessible(true);
            return method.invoke(target, ps);
        } catch (InvocationTargetException e) {
            return handlerError(e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }


    /**
     * 参数列表赋值
     */
    private Object[] buildParams(MethodModel model, Request request, Map<String, String> pathVariables) {

        // 参数赋值
        int pcount = model.getParameters().size();
        Object[] ps = new Object[pcount];

        if (0 == pcount) {
            return ps;
        }

        for (int i = 0; i < pcount; i++) {
            ParameterModel param = model.getParameters().get(i);
            Annotation a = param.getAnnotation();
            String value = null;

            if (a instanceof RequestParameter) {
                String name = ((RequestParameter) a).value();
                value = request.getParameters().get(name);
            } else if (a instanceof PathVariable) {
                String name = ((PathVariable) a).value();
                value = pathVariables.get(name);
            } else if (a instanceof RequestHeader) {
                String name = ((RequestHeader) a).value();
                value = request.getHeaders().get(name);
            } else if (a instanceof RequestBody) {
                // 整个是对象
                ps[i] = JSON.map2Object(param.getType(), request.getParameters());
                continue;
            } else if (param.getType() == Request.class) {
                ps[i] = request;
                continue;
            } else {
                // TODO
            }

            if (null != value) {
                if (param.getType() == String.class) {
                    ps[i] = value;
                } else if (param.getType() == java.util.Date.class) {
                    ps[i] = DateUtils.parse(value);
                } else {
                    // TODO 基本类型或普通参数构造函数实例化
                    Object object = ReflectUtils.constructorInstance(param.getType(), String.class, String.valueOf(value));
                    ps[i] = object;
                }
            }
        }
        return ps;
    }


    /**
     * 错误处理
     */
    private Object handlerError(Throwable e) {

        Class ec = e.getClass();
        // 先找异常一样的
        MethodModel methodModel = Forest._Exception_Method.get(ec);
        if (null != methodModel) {
            return doHandlerError(e, methodModel);
        }
        // 找父类
        for (Class clazz : Forest._Exception_Method.keySet()) {
            if (clazz.isAssignableFrom(ec)) {
                return doHandlerError(e, Forest._Exception_Method.get(clazz));
            }
        }
        return null;
    }

    private Object doHandlerError(Throwable e, MethodModel methodModel) {

        try {
            // TODO 子类异常

            Object target = BeanFactory.getBean(methodModel.getClazz());

            // 参数赋值
            int pcount = methodModel.getParameters().size();
            Object[] ps = new Object[pcount];

            if (0 < pcount) {

                for (int i = 0; i < pcount; i++) {
                    ParameterModel param = methodModel.getParameters().get(i);
                    if (param.getType() == e.getClass() || param.getType().isAssignableFrom(e.getClass())) {
                        ps[i] = e;
                    }
                }
            }
            Method method = methodModel.getMethod();
            method.setAccessible(true);
            return method.invoke(target, ps);
        } catch (Exception e1) {
            log.error(e1.getMessage(), e1);
        }
        return null;
    }

}
