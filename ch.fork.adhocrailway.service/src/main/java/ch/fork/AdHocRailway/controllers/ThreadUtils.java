package ch.fork.AdHocRailway.controllers;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by fork on 3/23/14.
 */
public class ThreadUtils {


    public static ThreadPoolExecutor createExecutorService(BlockingQueue<Runnable> queue) {
        return new ThreadPoolExecutor(1, 1, 60L, TimeUnit.SECONDS, queue);
    }

    public static RateLimiter createRateLimiter() {
        return RateLimiter.create(2);
    }

}
