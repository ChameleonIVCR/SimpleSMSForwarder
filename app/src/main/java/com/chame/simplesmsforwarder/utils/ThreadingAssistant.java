package com.chame.simplesmsforwarder.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadingAssistant {
    ScheduledExecutorService timeoutExecutor = Executors.newSingleThreadScheduledExecutor();
    ExecutorService smsThread = Executors.newSingleThreadExecutor();

    public void postTimeout(Runnable runnable, int seconds) {
        timeoutExecutor.schedule(runnable, seconds, TimeUnit.SECONDS);
    }

    public void postSms(Runnable runnable) {
        smsThread.submit(runnable);
    }
}
