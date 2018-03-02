package com.denghb.test.forest;

import com.denghb.eorm.Eorm;
import com.denghb.forest.annotation.Autowired;
import com.denghb.forest.annotation.Scheduled;
import com.denghb.forest.annotation.Service;
import com.denghb.utils.DateUtils;

import java.util.Date;

@Service
public class Task {



    @Scheduled(fixedRate = 1000L)
    public void run() {

        System.out.println("@Scheduled(fixedRate = 1000L) " + DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedDelay = 1000L)
    public void run2() {

        System.out.println("@Scheduled(fixedDelay = 1000L) " + DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
