package com.denghb.forest;

import com.denghb.eorm.Eorm;
import com.denghb.eorm.EormTxManager;
import com.denghb.forest.annotation.*;
import com.denghb.forest.model.MethodModel;
import com.denghb.forest.task.TaskManager;
import com.denghb.forest.utils.BeanFactory;
import com.denghb.log.Log;
import com.denghb.log.LogFactory;
import com.denghb.utils.ConfigUtils;
import com.denghb.utils.ReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Forest {

    private static Log log = LogFactory.getLog(Application.class);

    // GET/,
    static Map<String, MethodModel> _RESTful_Method = new ConcurrentHashMap<String, MethodModel>();
    // GET/*
    static Map<String, MethodModel> _Filter_Method = new ConcurrentHashMap<String, MethodModel>();

    // GET/*
    static Map<String, MethodModel> _Before_Method = new ConcurrentHashMap<String, MethodModel>();
    static Map<String, MethodModel> _After_Method = new ConcurrentHashMap<String, MethodModel>();

    static Map<Class, MethodModel> _Exception_Method = new ConcurrentHashMap<Class, MethodModel>();

    private static List<Class> services = new ArrayList<Class>();


    private static Object buildService(Class interfaceClass) {

        Object object = BeanFactory.getBean(interfaceClass);
        if (null != object) {
            return object;
        }

        if (interfaceClass == Eorm.class) {

            // TODO 数据库实例化
            String impl = ConfigUtils.getValue("eorm.impl", "com.denghb.eorm.impl.EormMySQLImpl");
            Class implClass = ReflectUtils.loadClass(impl);
            Object eorm = ReflectUtils.constructorInstance(implClass);
            BeanFactory.setBean(Eorm.class, (Eorm) eorm);

            EormTxManager.url = ConfigUtils.getValue("eorm.url");
            EormTxManager.username = ConfigUtils.getValue("eorm.username");
            EormTxManager.password = ConfigUtils.getValue("eorm.password");

            return eorm;
        }
        Class clazz = null;
        for (Class tmp : services) {
            if (interfaceClass.isAssignableFrom(tmp)) {
                clazz = tmp;
                break;
            }
        }
        Object target = ReflectUtils.constructorInstance(clazz);
        initField(target);

        final Object finalTarget = target;
        Object proxy = Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), new InvocationHandler() {

            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Method targetMethod = null;
                for (Method m : finalTarget.getClass().getMethods()) {
                    if (m.getName() == method.getName()) {
                        targetMethod = m;
                        break;
                    }
                }
                Transaction tx = targetMethod.getAnnotation(Transaction.class);
                Round round = targetMethod.getAnnotation(Round.class);// TODO
                try {
                    if (null != tx) {
                        EormTxManager.begin();
                    }
                    Object value = method.invoke(finalTarget, args);
                    if (null != tx) {
                        EormTxManager.commit();
                    }
                    return value;
                } catch (Exception e) {
                    if (null != tx) {
                        EormTxManager.rollback();
                    }
                    if (e instanceof InvocationTargetException) {
                        InvocationTargetException ite = (InvocationTargetException) e;
                        throw ite.getTargetException();
                    }
                    throw e;
                }
            }
        });
        BeanFactory.setBean(clazz, proxy);
        return proxy;
    }

    static void initField(Object target) {

        Field[] fields = target.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotations().length == 0) {
                continue;
            }

            Value value = field.getAnnotation(Value.class);
            if (null != value) {
                String string = ConfigUtils.getValue(value.value());
                ReflectUtils.setFieldValue(field, target, string);
            }

            Autowired autowired = field.getAnnotation(Autowired.class);
            if (null != autowired) {
                Class type = field.getType();
                Object obj = buildService(type);
                ReflectUtils.setFieldValue(field, target, obj);
            }
        }
    }

    public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {

        return clazz.getAnnotation(annotationType);
    }

    /**
     * 扫描流程
     * http://naotu.baidu.com/file/293540742be392029ce69808f1fecad3?token=d83402118ce9df11
     */
    static void init(Class start) {


        Set<Class> set = ReflectUtils.getSubClasses(start);
        List<Class> classes = new ArrayList<Class>();
        //
        for (Class clazz : set) {
            if (null != clazz.getAnnotation(Service.class)) {
                services.add(clazz);
            }

            if (null != clazz.getAnnotation(RESTful.class)) {
                classes.add(clazz);
            }
        }


        for (final Class clazz : classes) {
            final Object target = ReflectUtils.constructorInstance(clazz);

            BeanFactory.setBean(clazz, target);

            initField(target);

            // 获取方法
            List<Method> methods = ReflectUtils.getAllMethods(target.getClass());

            RESTful rest = findAnnotation(clazz, RESTful.class);
            String url = rest.value();

            // TODO 递归？
            RESTful rest2 = findAnnotation(clazz.getSuperclass(), RESTful.class);
            if (null != rest2) {
                url = rest2.value() + url;
            }

            for (Method method : methods) {
                if (method.getAnnotations().length == 0) {
                    continue;
                }

                Scheduled scheduled = method.getAnnotation(Scheduled.class);
                if (null != scheduled) {

                    if (method.getParameterTypes().length > 0) {
                        throw new ForestException("@Scheduled 只能无参方法 " + clazz.getName() + "." + method.getName());
                    }
                    // 任务管理器
                    TaskManager.register(target, method, scheduled);
                }

                GET get = method.getAnnotation(GET.class);
                if (null != get) {
                    addRESTful(GET.class.getSimpleName(), url + get.value(), new MethodModel(method));
                }
                POST post = method.getAnnotation(POST.class);
                if (null != post) {
                    addRESTful(POST.class.getSimpleName(), url + post.value(), new MethodModel(method));
                }
                PUT put = method.getAnnotation(PUT.class);
                if (null != put) {
                    addRESTful(PUT.class.getSimpleName(), url + put.value(), new MethodModel(method));
                }
                PATCH patch = method.getAnnotation(PATCH.class);
                if (null != patch) {
                    addRESTful(PATCH.class.getSimpleName(), url + patch.value(), new MethodModel(method));
                }
                DELETE delete = method.getAnnotation(DELETE.class);
                if (null != delete) {
                    addRESTful(DELETE.class.getSimpleName(), url + delete.value(), new MethodModel(method));
                }


                Filter filter = method.getAnnotation(Filter.class);
                if (null != filter) {

                    Class[] ms = filter.methods();
                    for (Class cl : ms) {
                        String key = cl.getSimpleName() + filter.value();
                        if (_Filter_Method.containsKey(key)) {
                            throw new IllegalArgumentException("Duplicate @" + cl.getSimpleName() + "(\"" + filter.value() + "\")");
                        }
                        _Filter_Method.put(key, new MethodModel(method));
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
                        _Before_Method.put(key, new MethodModel(method));
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
                        _After_Method.put(key, new MethodModel(method));
                    }
                }

                ExceptionHandler error = method.getAnnotation(ExceptionHandler.class);
                if (null != error) {
                    _Exception_Method.put(error.throwable(), new MethodModel(method));
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
