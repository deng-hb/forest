package com.denghb.test.forest;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RESTfulTest {

    static String HOST = "http://localhost:8888";


    @Test
    public void test1() {
        String res = HttpUtils.get(HOST + "/");
        System.out.println(res);
    }

    @Test
    public void test2() {
        String res = HttpUtils.get(HOST + "/2");
        System.out.println(res);
    }


    @Test
    public void testa2() {


        ExecutorService exec = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 10000; i++) {
            exec.execute(new Runnable() {
                public void run() {

                    String res = HttpUtils.get(HOST + "/2");
//                    String res = HttpUtils.get("http://192.168.65.223:9093/wechat/b");
                    System.out.println(res);

                }
            });
        }

        try {
            Thread.sleep(10 * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void test22() {

        ExecutorService exec = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 1000; i++) {
            exec.execute(new Runnable() {
                public void run() {

                    String res = HttpUtils.get(HOST + "/");
//                    String res = HttpUtils.get("http://192.168.65.223:9093/wechat/b");
                    System.out.println(res);

                }
            });
            exec.execute(new Runnable() {
                public void run() {
                    String res = HttpUtils.get(HOST + "/2");

//                    String res = HttpUtils.get("http://192.168.65.223:9093/wechat/a");
                    System.out.println(res);

                }
            });
        }

        try {
            Thread.sleep(10 * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
