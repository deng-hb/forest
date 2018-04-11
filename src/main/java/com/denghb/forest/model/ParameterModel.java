package com.denghb.forest.model;

import java.lang.annotation.Annotation;

// 一个参数只支持一个注解
public class ParameterModel {
    private Class type;// 参数类型

    private Annotation annotation;// 参数注解

    public ParameterModel(Class type, Annotation annotation) {
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