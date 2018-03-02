package com.denghb.forest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Scheduled
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scheduled {


    /**
     * TODO 使用三方？
     *
     * @return
     */
    String cron() default "";

    /**
     * 间隔多少毫秒执行一次
     *
     * @return
     */
    long fixedRate() default -1L;

    /**
     * 执行完成多少毫秒后执行
     *
     * @return
     */
    long fixedDelay() default -1L;
}
