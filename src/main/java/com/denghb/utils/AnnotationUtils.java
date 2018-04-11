package com.denghb.utils;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AnnotationUtils {

    private static Map<Class, Set<Annotation>> annotationCache = new ConcurrentHashMap<Class, Set<Annotation>>();

    public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
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
}
