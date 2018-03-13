package com.denghb.forest;


import com.denghb.eorm.Eorm;
import com.denghb.forest.annotation.*;
import com.denghb.forest.server.Request;
import com.denghb.forest.server.Response;
import com.denghb.forest.server.Server;
import com.denghb.forest.task.TaskManager;
import com.denghb.forest.utils.ClassUtils;
import com.denghb.json.JSON;
import com.denghb.log.Log;
import com.denghb.utils.ConfigUtils;
import com.denghb.utils.ReflectUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author denghb
 */
public class Application {

    private static Log log = ClassUtils.create(Log.class, Application.class);

    /**
     * 所有请求方法
     * <p>
     * <pre>
     * @GET("/user") -> <"GET/user",MethodInfo>
     * @Filter("/") -> <"Filter/",MethodInfo>
     * </pre>
     */
    static Map<String, MethodInfo> _OBJECT_METHOD = new ConcurrentHashMap<String, MethodInfo>();

    static Server _SERVER = new Server();

    private Application() {

    }

    public static void run(Class clazz, String[] args) {

        long start = System.currentTimeMillis();
        System.out.println("Starting ...");

        init(clazz);

        // 在start之前
        _SERVER.setHandler(new Server.Handler() {
            public Response execute(Request request) {


                String uri = request.getUri();

                // TODO 运行状态
                if (uri.equals("/status")) {

                    return Response.build(_OBJECT_METHOD);
                }

                log.info("{}\t{}", request.getMethod(), uri);

                // 过滤
                Object object = handlerFilter(request);
                if (null != object) {
                    return Response.build(object);
                }

                String path = request.getMethod() + uri;
                Application.MethodInfo info = _OBJECT_METHOD.get(path);
                Map<String, String> pathVariables = new HashMap<String, String>();

                if (null == info) {
                    // 参数在path上的匹配
                    for (String path1 : _OBJECT_METHOD.keySet()) {
                        buildPath(path1, path, pathVariables);
                        if (!pathVariables.isEmpty()) {
                            info = _OBJECT_METHOD.get(path1);
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

                    Object target = ClassUtils.create(info.getClazz());

                    // 执行path对应方法
                    Method method = info.getMethod();
                    method.setAccessible(true);
                    Object result = method.invoke(target, buildParams(info, request, pathVariables));

                    return Response.build(result);
                } catch (InvocationTargetException e) {
                    // 调用方法抛出异常
                    Object result = handlerError(e.getTargetException());
                    if (null != result) {
                        return Response.build(result);
                    }
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
        });


        int port = Server.DEFAULT_PORT;
        String port1 = ConfigUtils.getValue("port");
        if (null != port1) {
            port = Integer.parseInt(port1);
        }

        if (null != args) {
            for (String p : args) {
                if (p.startsWith("-p")) {
                    p = p.substring(p.indexOf("=") + 1, p.length()).trim();
                    port = Integer.parseInt(p);
                }
            }
        }

        System.out.println("Started (" + (System.currentTimeMillis() - start) + ")ms");

        _SERVER.start(port);

    }

    /**
     * 停止服务
     */
    public static void stop() {
        if (null != _SERVER) {
            _SERVER.shutdown();
            _SERVER = null;
        }
    }

    /**
     * 参数列表赋值
     */
    private static Object[] buildParams(Application.MethodInfo info, Request request, Map<String, String> pathVariables) {

        // 参数赋值
        int pcount = info.parameters.size();
        Object[] ps = new Object[pcount];

        if (0 == pcount) {
            return ps;
        }

        for (int i = 0; i < pcount; i++) {
            Param param = info.parameters.get(i);
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
            } else if (param.getType() == Eorm.class) {
                ps[i] = ClassUtils.create(param.getType());
                continue;
            } else {
                // TODO
            }

            if (null != value) {
                // TODO 日期格式
                if (param.getType() == String.class) {
                    ps[i] = value;
                } else {
                    // TODO 基本类型或普通参数构造函数实例化
                    Object object = ReflectUtils.constructorInstance(param.getType(), String.class,String.valueOf(value));
                    ps[i] = object;
                }
            }
        }
        return ps;
    }

    private static Object handlerFilter(Request request) {
        try {
            String key = Filter.class.getSimpleName() + request.getMethod() + request.getUri();

            Application.MethodInfo info = null;
            for (String key1 : _OBJECT_METHOD.keySet()) {
                boolean b = comparePath(key1, key);
                if (b) {
                    info = _OBJECT_METHOD.get(key1);
                    break;
                }

            }
            if (null == info) {
                return null;
            }

            Object target = ClassUtils.create(info.getClazz());
            Object[] ps = buildParams(info, request, null);

            Method method = info.getMethod();
            method.setAccessible(true);
            return method.invoke(target, ps);
        } catch (InvocationTargetException e1) {
            return handlerError(e1);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    /**
     * /* -> /x/xx || /xxx/xx/xx..  true
     * /*a/aa -> /xxxa/aa  true
     */
    private static boolean comparePath(String origin, String uri) {

        String[] tmp1s = origin.split("\\/");
        String[] tmp2s = uri.split("\\/");

        int len1 = tmp1s.length;
        int len2 = tmp2s.length;

        for (int i = 0; i < len1; i++) {
            String s1 = tmp1s[i];

            if ("*".equals(s1)) {
                return true;
            }
            String s2 = tmp2s[i];

            int start = s1.indexOf('*');
            if (-1 < start) {

                String s1start = s1.substring(0, start);

                if (!s2.startsWith(s1start)) {
                    return false;
                }
                int end = s1.lastIndexOf('*');
                String s1end = s1.substring(end + 1, s1.length());

                if (!s2.endsWith(s1end)) {
                    return false;
                }

            } else if (!s1.equals(s2)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 错误处理
     */
    private static Object handlerError(Throwable e) {


        try {
            // TODO 子类异常
            String key = ExceptionHandler.class.getSimpleName() + e.getClass().getSimpleName();
            Application.MethodInfo info = _OBJECT_METHOD.get(key);
            if (null == info) {
                return null;
            }

            Object target = ClassUtils.create(info.getClazz());

            // 参数赋值
            int pcount = info.parameters.size();
            Object[] ps = new Object[pcount];

            if (0 < pcount) {

                for (int i = 0; i < pcount; i++) {
                    Param param = info.parameters.get(i);
                    if (param.getType() == e.getClass()) {
                        ps[i] = e;
                    }
                }
            }
            Method method = info.getMethod();
            method.setAccessible(true);
            return method.invoke(target, ps);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    private static <A> void findAnnotations(Class<?> clazz, Set<Annotation> classSet) {

        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation a : annotations) {
            if (a instanceof Retention || a instanceof Documented || a instanceof Target) {
                continue;
            }

            Class<? extends Annotation> type = a.annotationType();
            if (type == clazz) {
                continue;
            }
            classSet.add(a);

            findAnnotations(a.annotationType(), classSet);

        }
    }

    private static Map<Class, Set<Annotation>> annotationCache = new ConcurrentHashMap<Class, Set<Annotation>>();

    private static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
        // 找到所有的注解

        Set<Annotation> classSet = annotationCache.get(clazz);
        if (null == classSet) {
            classSet = new HashSet<Annotation>();
            findAnnotations(clazz, classSet);
            annotationCache.put(clazz, classSet);
        }

        for (Annotation annotation : classSet) {
            if (annotation.annotationType() == annotationType) {
                return (A) annotation;
            }
        }

        return null;
    }

    private static void init(Class clazz) {

        Set<Class> set = ReflectUtils.getSubClasses(clazz);
        //
        for (Class c : set) {

            Service service = findAnnotation(c, Service.class);
            if (null == service) {
                continue;
            }
            Object target = ClassUtils.create(c);
            // 字段
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                if (field.getAnnotations().length == 0) {
                    continue;
                }

                Value value = field.getAnnotation(Value.class);
                if (null != value) {
                    String string = ConfigUtils.getValue(value.name());
                    ReflectUtils.setFieldValue(field, target, string);
                }

                Autowired autowired = field.getAnnotation(Autowired.class);
                if (null != autowired) {
                    Class type = field.getType();
                    Object object = null;
                    if (type.isInterface()) {
                        // 查找对应实现
                        for (Class ccc : set) {
                            if (type.isAssignableFrom(ccc)) {
                                object = ClassUtils.create(ccc);
                                break;
                            }
                        }
                    }
                    if (null == object) {
                        object = ClassUtils.create(type);
                    }
                    ReflectUtils.setFieldValue(field, target, object);
                }
            }

            // 获取方法
            List<Method> methods = ReflectUtils.getAllMethods(c);

            for (Method method : methods) {
                if (method.getAnnotations().length == 0) {
                    continue;
                }
                ExceptionHandler error = method.getAnnotation(ExceptionHandler.class);
                if (null != error) {
                    add(ExceptionHandler.class.getSimpleName(), error.throwable().getSimpleName(), new MethodInfo(c, method));
                }

                Scheduled scheduled = method.getAnnotation(Scheduled.class);
                if (null != scheduled) {

                    if (method.getParameterTypes().length > 0) {
                        throw new ForestException("@Scheduled 只能无参方法 " + c.getName() + "." + method.getName());
                    }
                    // 任务管理器
                    TaskManager.register(target, method, scheduled);
                }

                Filter before = method.getAnnotation(Filter.class);
                if (null != before) {

                    Class[] ms = before.methods();
                    for (Class cl : ms) {
                        // GET/*  POST/*
                        String path = cl.getSimpleName() + before.value();
                        add(Filter.class.getSimpleName(), path, new MethodInfo(c, method));
                    }
                }
            }

            RESTful rest = findAnnotation(c, RESTful.class);
            if (null == rest) {
                continue;
            }
            String url = rest.value();

            for (Method method : methods) {
                if (method.getAnnotations().length == 0) {
                    continue;
                }
                GET get = method.getAnnotation(GET.class);
                if (null != get) {
                    add(GET.class.getSimpleName(), url + get.value(), new MethodInfo(c, method));
                }
                POST post = method.getAnnotation(POST.class);
                if (null != post) {
                    add(POST.class.getSimpleName(), url + post.value(), new MethodInfo(c, method));
                }
                PUT put = method.getAnnotation(PUT.class);
                if (null != put) {
                    add(PUT.class.getSimpleName(), url + put.value(), new MethodInfo(c, method));
                }
                PATCH patch = method.getAnnotation(PATCH.class);
                if (null != patch) {
                    add(PATCH.class.getSimpleName(), url + patch.value(), new MethodInfo(c, method));
                }
                DELETE delete = method.getAnnotation(DELETE.class);
                if (null != delete) {
                    add(DELETE.class.getSimpleName(), url + delete.value(), new MethodInfo(c, method));
                }
            }
        }

        TaskManager.start();
    }

    // 添加到方法对象
    private static void add(String method, String path, MethodInfo info) {

        String key = method + path;
        if (_OBJECT_METHOD.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate @" + method + "(\"" + path + "\")");
        }
        _OBJECT_METHOD.put(key, info);
    }

    /**
     * 正则搞不懂，硬解析
     *
     * @param path1 /x/ss{id}
     * @param path2 /x/ss234
     *              {id=234}
     */
    private static void buildPath(String path1, String path2, Map<String, String> pathVar) {
        int start = path1.indexOf('{');
        if (-1 == start) {
            return;
        }

        String tmp1 = path1.substring(0, start);
        if (!path2.startsWith(tmp1)) {
            return;// 不属于
        }
        String[] tmp1s = path1.substring(start, path1.length()).split("\\/");
        String[] tmp2s = path2.substring(start, path2.length()).split("\\/");
        if (tmp1s.length != tmp2s.length || 0 == tmp1s.length) {
            return;
        }

        // 假定他们是一样的
        for (int i = 0; i < tmp1s.length; i++) {
            String key = tmp1s[i];
            String value = tmp2s[i];

            int start1 = key.indexOf('{');
            int end1 = key.indexOf('}');

            if (0 != start1 || end1 != key.length() - 1) {
                // 需要掐头去尾
                if (start1 > 0) {
                    String startStr = key.substring(0, start1);
                    if (!value.startsWith(startStr)) {
                        pathVar.clear();
                        return;// 不匹配
                    }
                    value = value.substring(start1, value.length());
                }

                // 去尾
                if (end1 != key.length() - 1) {
                    String endKeyStr = key.substring(end1 + 1, key.length());
                    if (!value.endsWith(endKeyStr)) {
                        pathVar.clear();
                        return;// 不匹配
                    }
                    value = value.substring(0, value.indexOf(endKeyStr));
                }
            }

            key = key.substring(start1 + 1, end1);
            pathVar.put(key, value);

        }

    }

    private static class MethodInfo {

        private Class clazz;

        private Method method;

        private List<Param> parameters;// 所有参数名

        public MethodInfo(Class clazz, Method method) {
            this.clazz = clazz;
            this.method = method;

            this.parameters = new ArrayList<Param>();

            // 解析参数适配1.5、1.8有新方法
            Class<?>[] types = method.getParameterTypes();
            int length = types.length;
            if (length == 0) {
                return;
            }

            for (int i = 0; i < length; i++) {

                Class pc = types[i];
                this.parameters.add(new Param(pc, null));// 先把类型填入

                Annotation[] parameterAnnotations = method.getParameterAnnotations()[i];
                for (Annotation annotation : parameterAnnotations) {
                    this.parameters.set(i, new Param(pc, annotation));
                }
            }

        }

        public Class getClazz() {
            return clazz;
        }

        public void setClazz(Class clazz) {
            this.clazz = clazz;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public List<Param> getParameters() {
            return parameters;
        }

        public void setParameters(List<Param> parameters) {
            this.parameters = parameters;
        }
    }


    // 一个参数只支持一个注解
    private static class Param {
        private Class type;// 参数类型

        private Annotation annotation;// 参数注解

        public Param(Class type, Annotation annotation) {
            this.type = type;
            this.annotation = annotation;
        }

        public Class getType() {
            return type;
        }

        public void setType(Class type) {
            this.type = type;
        }

        public Annotation getAnnotation() {
            return annotation;
        }

        public void setAnnotation(Annotation annotation) {
            this.annotation = annotation;
        }
    }


}
