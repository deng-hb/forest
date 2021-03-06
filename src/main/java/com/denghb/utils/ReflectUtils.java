package com.denghb.utils;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * 反射工具类
 */
public class ReflectUtils {

    /**
     * 获得实体类的所有属性（该方法递归的获取当前类及父类中声明的字段。最终结果以list形式返回）
     */
    public static List<Field> getFields(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        List<Field> fields = new ArrayList<Field>();
        Field[] classFields = clazz.getDeclaredFields();
        fields.addAll(Arrays.asList(classFields));

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != Object.class) {
            List<Field> superClassFields = getFields(superclass);
            fields.addAll(superClassFields);
        }
        return fields;
    }

    /**
     * 通过属性对象和实体对象获取字段的值
     */
    public static Object getFieldValue(Field field, Object object) {
        if (field == null || object == null) {
            return null;
        }

        try {
            field.setAccessible(true);
            return field.get(object);// 获取字段的值
        } catch (Exception e) {
            throw new RuntimeException("Can't get field (" + field.getName() + ") value from object " + object, e);
        }
    }

    /**
     * 将值保存到实体对象的指定属性中
     */
    public static void setFieldValue(Field field, Object object, Object value) {
        try {

            Class type = field.getType();

            // number | boolean | string 类型有字符串构造函数
            if (Number.class.isAssignableFrom(type) || type == Boolean.class || CharSequence.class.isAssignableFrom(type)) {
                value = ReflectUtils.constructorInstance(type, String.class, String.valueOf(value));
            } else if (type.isPrimitive()) {
                if (int.class == type) {
                    value = Integer.parseInt((String) value);
                } else if (long.class == type) {
                    value = Long.parseLong((String) value);
                } else if (float.class == type) {
                    value = Float.parseFloat((String) value);
                } else if (double.class == type) {
                    value = Double.parseDouble((String) value);
                }
            }

            // TODO Date\基本数据类型

            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            throw new RuntimeException("Can't set value（" + value + "）to instance " + object.getClass().getName() + "." + field.getName(), e);
        }
    }


    /**
     * 获取所有的方法
     *
     * @param clazz
     * @return
     */
    public static List<Method> getAllMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<Method>();
        while (!clazz.equals(Object.class)) {
            for (Method m : clazz.getDeclaredMethods()) {
                methods.add(m);
            }
            clazz = clazz.getSuperclass();
        }
        return methods;
    }

    /**
     * 根据实体类创建实体对象
     */
    public static Object constructorInstance(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Object class mustn't be null");
        }

        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance（" + clazz.getName() + "）  by reflect!", e);
        }
    }

    /**
     * 有参构造创建对象
     */
    public static Object constructorInstance(Class<?> clazz, Class type, Object value) {
        return constructorInstance(clazz, new Class[]{type}, new Object[]{value});
    }

    public static Object constructorInstance(Class<?> clazz, Class[] types, Object[] values) {
        if (clazz == null) {
            throw new IllegalArgumentException("Object class mustn't be null");
        }
        try {
            return clazz.getConstructor(types).newInstance(values);
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance（" + clazz.getName() + "）  by reflect!", e);
        }
    }

    /**
     * 获取子类
     *
     * @param clazz
     * @return
     */
    public static Set<Class> getSubClasses(Class clazz) {

        // 第一个class类的集合
        Set<Class> classes = new LinkedHashSet<Class>();
        // 获取包的名字 并进行替换
        String packageName = clazz.getName();
        packageName = packageName.substring(0, packageName.lastIndexOf('.'));

        String packageDirName = packageName.replace('.', '/');

        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    //System.err.println("file类型的扫描");
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, classes);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    //System.err.println("jar类型的扫描");
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                if (idx != -1) {
                                    // 如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        // 去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        Class clazz1 = loadClass(packageName + '.' + className);
                                        // 添加到classes
                                        classes.add(clazz1);
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        // log.error("在扫描用户定义视图时从jar包获取文件出错");
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName
     * @param packagePath
     * @param classes
     */
    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, Set<Class> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            // log.warn("用户定义包名 " + packageName + " 下没有任何文件");
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file) {
                return file.isDirectory() || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                // 添加到集合中去
                Class clazz = loadClass(packageName + '.' + className);
                classes.add(clazz);
            }
        }
    }

    public static Class loadClass(String className) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
