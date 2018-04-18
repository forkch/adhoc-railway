package ch.fork.AdHocRailway.controllers;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.*;

/**
 * Created by fork on 18.01.15.
 */
public class TaskExecutor {
    private final RateLimiter rateLimiter;
    private final ExecutorService executorService;
    private final ExecutorService emergencyExecutorService;

    private final BlockingQueue<Runnable> tasksQueue = new ArrayBlockingQueue<>(50);

    public TaskExecutor() {
        executorService = ThreadUtils.createExecutorService(tasksQueue);
        BlockingQueue<Runnable> emergencyTasksQueue = new ArrayBlockingQueue<>(50);
        emergencyExecutorService = ThreadUtils.createExecutorService(emergencyTasksQueue);
        rateLimiter = ThreadUtils.createRateLimiter();
    }

    public void enqueueTask(final Runnable task) {
        try {
            executorService.execute(task);
        } catch(RejectedExecutionException x) {
            throw new RuntimeException("Slow down buddy :-)");
        }
    }

    public void cancelTasks() {
        tasksQueue.clear();
    }

    public int pendingTaskCount() {
        return tasksQueue.size();
    }

    public void aquireRateLock() {
        rateLimiter.acquire();
    }

    public void enqueueEmergencyTask(Runnable runnable) {
        emergencyExecutorService.execute(runnable);
    }
}
