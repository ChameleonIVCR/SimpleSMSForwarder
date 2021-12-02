package com.chame.simplesmsforwarder.utils;

import java.util.concurrent.*;


public class ThreadingAssistant {
    private final ScheduledExecutorService timeoutExecutor = Executors.newScheduledThreadPool(2);
    private final ScheduledExecutorService readyTimeout = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService smsThread = Executors.newSingleThreadExecutor();

    public void postTimeout(Runnable runnable, int seconds) {
        timeoutExecutor.schedule(runnable, seconds, TimeUnit.SECONDS);
    }

    public void postReadyTimeout(Runnable runnable, long millis) {
        readyTimeout.schedule(runnable, millis, TimeUnit.MILLISECONDS);
    }

    public void socketHeartBeat(Runnable runnable, int seconds) {
        timeoutExecutor.scheduleAtFixedRate(runnable, 0, seconds, TimeUnit.SECONDS);
    }

    public void postSms(Runnable runnable) {
        smsThread.submit(runnable);
    }
}
