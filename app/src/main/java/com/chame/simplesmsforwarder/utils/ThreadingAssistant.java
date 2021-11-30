package com.chame.simplesmsforwarder.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadingAssistant {
    ScheduledExecutorService timeoutExecutor = Executors.newScheduledThreadPool(2);
    ExecutorService smsThread = Executors.newSingleThreadExecutor();

    public void postTimeout(Runnable runnable, int seconds) {
        timeoutExecutor.schedule(runnable, seconds, TimeUnit.SECONDS);
    }

    public void socketHeartBeat(Runnable runnable, int seconds) {
        timeoutExecutor.scheduleAtFixedRate(runnable, 0, seconds, TimeUnit.SECONDS);
    }

    public void postSms(Runnable runnable) {
        smsThread.submit(runnable);
    }
}
