package com.michelle.common;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author michelle.min
 */

public class ZKScheduleTask {
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public ZKScheduleTask(int index) {
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getId());
                System.out.println(index + " task start at:" + System.currentTimeMillis());
                System.out.println(index + " task is executing!");
                System.out.println(index + " task success at:" + System.currentTimeMillis());
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    public void cloase() {
        scheduledExecutorService.shutdown();
    }

    public static void main(String[] args) {
        ZKScheduleTask zkScheduleTask = new ZKScheduleTask(1);
        ZKScheduleTask zkScheduleTask1 = new ZKScheduleTask(2);
    }
}
