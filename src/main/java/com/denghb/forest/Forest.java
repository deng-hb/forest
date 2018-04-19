package com.denghb.forest;

import com.denghb.eorm.EormTxManager;
import com.denghb.forest.annotation.*;
import com.denghb.forest.model.MethodModel;
import com.denghb.forest.task.TaskManager;
import com.denghb.forest.utils.AnnotationUtils;
import com.denghb.forest.utils.ClassUtils;
import com.denghb.utils.ConfigUtils;
import com.denghb.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Forest {

    // GET/,
    static Map<String, MethodModel> _RESTful_Method = new ConcurrentHashMap<String, MethodModel>();
    // GET/*
    static Map<String, MethodModel> _Filter_Method = new ConcurrentHashMap<String, MethodModel>();

    // GET/*
    static Map<String, MethodModel> _Before_Method = new ConcurrentHashMap<String, MethodModel>();
    static Map<String, MethodModel> _After_Method = new ConcurrentHashMap<String, MethodModel>();

    static Map<Class, MethodModel> _Exception_Method = new ConcurrentHashMap<Class, MethodModel>();

    static void init(Class clazz) {

        Set<Class> set = ReflectUtils.getSubClasses(clazz);
        List<Class> services = new ArrayList<Class>();
        //
        for (Class c : set) {
            Service service = AnnotationUtils.findAnnotation(c, Service.class);
            if (null != service) {
                services.add(c);
            }
        }

        for (Class c : services) {
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
                        for (Class ccc : services) {
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

                Scheduled scheduled = method.getAnnotation(Scheduled.class);
                if (null != scheduled) {

                    if (method.getParameterTypes().length > 0) {
                        throw new ForestException("@Scheduled 只能无参方法 " + c.getName() + "." + method.getName());
                    }
                    // 任务管理器
                    TaskManager.register(target, method, scheduled);
                }

                Transaction tx = method.getAnnotation(Transaction.class);
                if (null != tx) {

                    Proxy.newProxyInstance(c.getClassLoader(), c.getInterfaces(), new InvocationHandler() {

                        public Object invoke(Object proxy, Method method, Object[] args)
                                throws Throwable {
                            try {
                                EormTxManager.begin();
                                Object value = method.invoke(proxy, args);
                                EormTxManager.commit();
                                return value;
                            } catch (Exception e) {
                                EormTxManager.rollback();
                                throw e;
                            }
                        }

                    });
                }

            }

            RESTful rest = (RESTful) c.getAnnotation(RESTful.class);
            if (null == rest) {
                continue;
            }
            String url = rest.value();

            // TODO 递归？
            RESTful rest2 = (RESTful) c.getSuperclass().getAnnotation(RESTful.class);
            if (null != rest2) {
                url = rest2.value() + url;
            }

            for (Method method : methods) {
                if (method.getAnnotations().length == 0) {
                    continue;
                }

                GET get = method.getAnnotation(GET.class);
                if (null != get) {
                    addRESTful(GET.class.getSimpleName(), url + get.value(), new MethodModel(c, method));
                }
                POST post = method.getAnnotation(POST.class);
                if (null != post) {
                    addRESTful(POST.class.getSimpleName(), url + post.value(), new MethodModel(c, method));
                }
                PUT put = method.getAnnotation(PUT.class);
                if (null != put) {
                    addRESTful(PUT.class.getSimpleName(), url + put.value(), new MethodModel(c, method));
                }
                PATCH patch = method.getAnnotation(PATCH.class);
                if (null != patch) {
                    addRESTful(PATCH.class.getSimpleName(), url + patch.value(), new MethodModel(c, method));
                }
                DELETE delete = method.getAnnotation(DELETE.class);
                if (null != delete) {
                    addRESTful(DELETE.class.getSimpleName(), url + delete.value(), new MethodModel(c, method));
                }


                Filter filter = method.getAnnotation(Filter.class);
                if (null != filter) {

                    Class[] ms = filter.methods();
                    for (Class cl : ms) {
                        String key = cl.getSimpleName() + filter.value();
                        if (_Filter_Method.containsKey(key)) {
                            throw new IllegalArgumentException("Duplicate @" + cl.getSimpleName() + "(\"" + filter.value() + "\")");
                        }
                        _Filter_Method.put(key, new MethodModel(c, method));
                    }
                }

                Before before = method.getAnnotation(Before.class);
                if (null != before) {

                    Class[] ms = before.methods();
                    for (Class cl : ms) {

                        String key = cl.getSimpleName() + before.value();
                        if (_Before_Method.containsKey(key)) {
                            throw new IllegalArgumentException("Duplicate @" + cl.getSimpleName() + "(\"" + before.value() + "\")");
                        }
                        _Before_Method.put(key, new MethodModel(c, method));
                    }
                }

                After after = method.getAnnotation(After.class);
                if (null != after) {

                    Class[] ms = after.methods();
                    for (Class cl : ms) {

                        String key = cl.getSimpleName() + after.value();
                        if (_After_Method.containsKey(key)) {
                            throw new IllegalArgumentException("Duplicate @" + cl.getSimpleName() + "(\"" + after.value() + "\")");
                        }
                        _After_Method.put(key, new MethodModel(c, method));
                    }
                }

                ExceptionHandler error = method.getAnnotation(ExceptionHandler.class);
                if (null != error) {
                    _Exception_Method.put(error.throwable(), new MethodModel(c, method));
                }
            }
        }

        TaskManager.start();
    }

    // 添加到方法对象
    private static void addRESTful(String method, String path, MethodModel info) {

        String key = method + path;
        if (_RESTful_Method.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate @" + method + "(\"" + path + "\")");
        }
        _RESTful_Method.put(key, info);
    }


}
