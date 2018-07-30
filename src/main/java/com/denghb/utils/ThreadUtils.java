package com.denghb.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadUtils {


    private static ExecutorService service = Executors.newFixedThreadPool(20, new ThreadFactory() {
        AtomicInteger atomic = new AtomicInteger();

        public Thread newThread(Runnable r) {
            return new Thread(r, "serv-" + this.atomic.getAndIncrement());
        }

    });

    public static ExecutorService getService() {
        return service;
    }

    public static void submit(Runnable task) {
        service.submit(task);
    }
}
