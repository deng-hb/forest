package com.denghb.forest.annotation;

import java.lang.annotation.*;

/**
 * 自动装配
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {

    String name() default "";// class simple name
}
