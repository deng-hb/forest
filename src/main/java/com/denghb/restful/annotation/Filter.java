package com.denghb.restful.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 过滤器
 * 返回null表示放行
 *
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Filter {
    String value() default "/*";

    Class[] method() default {GET.class, POST.class, DELETE.class};
}
