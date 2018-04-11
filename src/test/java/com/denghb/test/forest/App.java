package com.denghb.test.forest;


import com.denghb.eorm.Eorm;
import com.denghb.forest.Application;
import com.denghb.forest.ForestException;
import com.denghb.forest.annotation.*;
import com.denghb.forest.utils.ClassUtils;
import com.denghb.log.Log;
import com.denghb.log.LogFactory;
import com.denghb.test.forest.service.UserService;

import java.io.IOException;
import java.util.Date;

@RESTful
public class App {
    private static Log log = LogFactory.getLog(Application.class);

    public static void main(String[] args) throws IOException {
        Application.run(App.class, args);
    }

    @Autowired
    private UserService userService;

    @Autowired
    private Eorm eorm;

    @Value(name = "ab")
    private String ab;

    int a = 0;

    @WebSocket
    void webSocket(String message) {

    }

    // @Scheduled(fixedRate = 10 * 1000)
    void run() {
        userService.create();
    }

    //@ExceptionHandler(throwable = ForestException.class)
    String error(ForestException e) {
        e.printStackTrace();
        return "捕获自定义错误1";
    }
    @ExceptionHandler
    String error(Exception e) {
        e.printStackTrace();
        return "捕获错误";
    }

    @GET("/error")
    void throwError() {
        throw new ForestException("aa");
    }

    @GET
    String home() {

        System.out.println(ab);

        System.out.println(++a);
        eorm.doTx(new Eorm.Handler() {
            public void doTx(Eorm eorm) {

                User user = new User();
                user.setMobile("123123" + a);
                user.setName("张" + a);
                user.setCreatedTime(new Date());
                eorm.insert(user);

            }
        });
        Integer count = eorm.selectOne(Integer.class, "select count(*) from user");
        log.info(String.valueOf(count));

        Book book = ClassUtils.create(Book.class, 1, "1", 1.1);

        return "Hello world!" + a;
    }

    int a2 = 0;

    @GET("/2")
    String home2(@RequestParameter("name") String name) {
        return ++a2 + "Hello world!" + name;
    }
}
