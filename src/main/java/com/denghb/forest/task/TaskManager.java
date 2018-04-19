package com.denghb.forest.task;

import com.denghb.eorm.EormTxManager;
import com.denghb.forest.annotation.Scheduled;
import com.denghb.forest.annotation.Transaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class TaskManager {

    private static ScheduledExecutorService service = Executors.newScheduledThreadPool(10);

    private static Queue<Task> QUEUE = new ConcurrentLinkedQueue<Task>();

    public static void register(final Object object, final Method method, Scheduled scheduled) {

        QUEUE.add(new Task(object, method, scheduled));

    }

    public static void start() {

        for (Task task : QUEUE) {

            addFixedRate(task.object, task.method, task.scheduled.fixedRate());

            addFixedDelay(task.object, task.method, task.scheduled.fixedDelay());
        }

    }

    private static void addFixedRate(final Object object, final Method method, long ms) {
        if (ms <= 0) {
            return;
        }
        service.scheduleAtFixedRate(new Runnable() {
            public void run() {
                doMethod(object, method);
            }
        }, 0, ms, TimeUnit.MILLISECONDS);
    }


    private static void addFixedDelay(final Object object, final Method method, long ms) {
        if (ms <= 0) {
            return;
        }
        service.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                doMethod(object, method);
            }
        }, 0, ms, TimeUnit.MILLISECONDS);
    }

    private static void doMethod(Object object, Method method) {
        boolean tx = null != method.getAnnotation(Transaction.class);
        try {
            if (tx) {
                EormTxManager.begin();

            }
            method.setAccessible(true);
            method.invoke(object);
            if (tx) {
                EormTxManager.commit();

            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            if (tx) {
                try {
                    EormTxManager.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static class Task {
        private Object object;
        private Method method;
        private Scheduled scheduled;

        public Task(Object object, Method method, Scheduled scheduled) {
            this.object = object;
            this.method = method;
            this.scheduled = scheduled;
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public Scheduled getScheduled() {
            return scheduled;
        }

        public void setScheduled(Scheduled scheduled) {
            this.scheduled = scheduled;
        }
    }
}
