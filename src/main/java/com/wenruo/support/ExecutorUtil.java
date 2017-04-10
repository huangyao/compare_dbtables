package com.wenruo.support;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by huangyao on 2017/4/8.
 */
public class ExecutorUtil {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private ExecutorUtil() {
    }

    public static Future<?> submit(Runnable task) {
        return EXECUTOR.submit(task);
    }

    public static <T> Future<T> submit(Callable<T> task) {
        return EXECUTOR.submit(task);
    }
}
