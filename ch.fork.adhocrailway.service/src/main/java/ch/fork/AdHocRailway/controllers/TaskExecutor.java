package ch.fork.AdHocRailway.controllers;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.ExecutorService;

/**
 * Created by fork on 18.01.15.
 */
public class TaskExecutor {
    private final RateLimiter rateLimiter;
    private final ExecutorService executorService;
    private final ExecutorService emergencyExecutorService;

    public TaskExecutor() {
        executorService = ThreadUtils.createExecutorService();
        emergencyExecutorService = ThreadUtils.createExecutorService();
        rateLimiter = ThreadUtils.createRateLimiter();
    }

    public void enqueueTask(final Runnable task) {
        executorService.execute(task);
    }

    public void aquireRateLock() {
        rateLimiter.acquire();
    }

    public void enqueueEmergencyTask(Runnable runnable) {
        emergencyExecutorService.execute(runnable);
    }
}
