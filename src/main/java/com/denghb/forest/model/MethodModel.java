package com.denghb.forest.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodModel {

    private Class clazz;

    private Method method;

    private List<ParameterModel> parameters;// 所有参数名

    public MethodModel(Method method) {
        this.clazz = method.getDeclaringClass();
        this.method = method;

        this.parameters = new ArrayList<ParameterModel>();

        // 解析参数适配1.5、1.8有新方法
        Class<?>[] types = method.getParameterTypes();
        int length = types.length;
        if (length == 0) {
            return;
        }

        for (int i = 0; i < length; i++) {

            Class pc = types[i];
            this.parameters.add(new ParameterModel(pc, null));// 先把类型填入

            Annotation[] parameterAnnotations = method.getParameterAnnotations()[i];
            for (Annotation annotation : parameterAnnotations) {
                this.parameters.set(i, new ParameterModel(pc, annotation));
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

    public List<ParameterModel> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterModel> parameters) {
        this.parameters = parameters;
    }

}