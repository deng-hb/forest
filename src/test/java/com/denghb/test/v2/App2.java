package com.denghb.test.v2;

import com.denghb.eorm.Eorm;
import com.denghb.forest.Application;
import com.denghb.forest.annotation.Autowired;
import com.denghb.forest.annotation.GET;
import com.denghb.forest.annotation.POST;
import com.denghb.forest.annotation.RESTful;
import com.denghb.http.Request;
import com.denghb.json.JSON;
import com.denghb.log.Log;
import com.denghb.log.LogFactory;

import java.util.Properties;

@RESTful
public class App2 {
    private static Log log = LogFactory.getLog(App2.class);

    public static void main(String[] args) {
        Properties pro = System.getProperties();
        System.out.println(pro);
        System.out.println(pro.getProperty("file.encoding"));

        Application.run(App2.class, args);
    }

    private int i = 0;

    @Autowired
    private Eorm db;

    @GET("/")
    public String index() {
        log.info("" + i++);
        db.execute("insert into user(age,updated_time) values (23,now(3))");
        return "HI";
    }

    @POST("/")
    public String post(Request request) {
        return "HI" + JSON.toJSON(request);
    }
}
