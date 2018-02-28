package com.denghb.test.forest;

import com.denghb.eorm.Eorm;
import com.denghb.restful.Application;
import com.denghb.restful.annotation.Autowired;
import com.denghb.restful.annotation.GET;
import com.denghb.restful.annotation.RESTful;
import com.denghb.restful.annotation.Value;
import com.denghb.utils.LogUtils;

@RESTful
public class App {

    public static void main(String[] args) {
        Application.run(App.class, args);
    }

    @Autowired
    private Eorm eorm;

    @Value(name = "ab")
    private String ab;

    int a = 0;

    @GET("/")
    String home() {

        System.out.println(ab);

        System.out.println(++a);
        Integer count = eorm.selectOne(Integer.class, "select count(*) from user");

        LogUtils.info(this.getClass(), String.valueOf(count));
        return "Hello world!" + a;
    }

    @GET("/2")
    String home2() {
        return "Hello world!2";
    }
}
