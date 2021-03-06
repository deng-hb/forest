package com.denghb.forest.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在什么请求之前
 * 返回null表示放行
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Filter {

    String value() default "/*";

    Class[] methods() default {GET.class, POST.class, DELETE.class, PATCH.class, PUT.class};
}
