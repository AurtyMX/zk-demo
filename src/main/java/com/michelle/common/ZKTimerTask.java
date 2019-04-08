package com.michelle.common;


import java.util.Timer;
import java.util.TimerTask;

/**
 * @author michelle.min
 */

public class ZKTimerTask extends TimerTask {
    @Override
    public void run() {
        int index = 1;
        if (index == 1) {
            throw new NullPointerException("index=" + index);
        }
        System.out.println(Thread.currentThread().getId());
        System.out.println(index + " task start at:" + System.currentTimeMillis());
        System.out.println(index + " task is executing!");
        System.out.println(index + " task success at:" + System.currentTimeMillis());
    }

    public static void main(String[] args) {
        ZKTimerTask zkTimerTask = new ZKTimerTask();
        TimerTask zkTimerTask2 = new TimerTask() {
            @Override
            public void run() {
                int index = 2;
                System.out.println(Thread.currentThread().getId());
                System.out.println(index + " task start at:" + System.currentTimeMillis());
                System.out.println(index + " task is executing!");
                System.out.println(index + " task success at:" + System.currentTimeMillis());
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(zkTimerTask, 0L, 10 * 1000L);
        timer.scheduleAtFixedRate(zkTimerTask2, 0L, 10 * 1000L);
    }

}
