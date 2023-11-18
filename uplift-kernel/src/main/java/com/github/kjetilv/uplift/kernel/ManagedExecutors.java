package com.github.kjetilv.uplift.kernel;

import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

public final class ManagedExecutors {

    public static void configure(int coreThreads, int queueLength) {
        configure(coreThreads, queueLength, Duration.ofSeconds(1));
    }

    public static ExecutorService backgroundLogging() {
        return executor("logger", 1);
    }

    public static void configure(int coreThreads, int queueLength, int secondsKeepAlive) {
        configure(coreThreads, queueLength, Duration.ofSeconds(secondsKeepAlive));
    }

    public static void configure(int coreThreads, int queueLength, Duration keepAlive) {
        CORE_THREADS_DEFAULT.set(coreThreads);
        MAX_QUEUE_LENGTH_DEFAULT.set(queueLength);
        KEEP_ALIVE_TIME.set(keepAlive);
    }

    public static ExecutorService executor(String prefix) {
        return executor(prefix, CORE_THREADS_DEFAULT.get());
    }

    public static ExecutorService executor(String prefix, int coreThreads) {
        return executor(prefix, coreThreads, MAX_QUEUE_LENGTH_DEFAULT.get());
    }

    public static ExecutorService executor(String prefix, int coreThreads, int maxQueueLength) {
        if (coreThreads < 1) {
            throw new IllegalArgumentException("Invalid core threads: " + coreThreads);
        }
        if (maxQueueLength < 1) {
            throw new IllegalArgumentException("Invalid queue length: " + maxQueueLength);
        }
        if (coreThreads == 1) {
            return Executors.newSingleThreadExecutor(threadFactory(prefix));
        }
        return new ThreadPoolExecutor(
            coreThreads,
            coreThreads,
            KEEP_ALIVE_TIME.get().toMillis(),
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(maxQueueLength),
            threadFactory(prefix),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    private ManagedExecutors() {
    }

    private static final AtomicReference<Duration> KEEP_ALIVE_TIME = new AtomicReference<>(Duration.ofSeconds(10));

    private static final AtomicInteger CORE_THREADS_DEFAULT = new AtomicInteger(4);

    private static final AtomicInteger MAX_QUEUE_LENGTH_DEFAULT = new AtomicInteger(10);

    private static final LongAdder THREAD_COUNT = new LongAdder();

    private static ThreadFactory threadFactory(String prefix) {
        return runnable -> {
            String name = nextName(prefix);
            return new Thread(
                () -> {
                    try {
                        runnable.run();
                    } catch (Throwable e) {
                        LoggerFactory.getLogger(ManagedExecutors.class)
                            .warn("{}: Uncaught exception {}", name, runnable, e);
                    }
                },
                name
            );
        };
    }

    private static String nextName(String prefix) {
        try {
            return prefix + (prefix.endsWith("-") ? "" : "-") + THREAD_COUNT.longValue();
        } finally {
            THREAD_COUNT.increment();
        }
    }
}
