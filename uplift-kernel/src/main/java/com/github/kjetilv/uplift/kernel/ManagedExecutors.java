package com.github.kjetilv.uplift.kernel;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

import com.github.kjetilv.uplift.flogs.DefaultLogFormatter;
import org.slf4j.LoggerFactory;

public final class ManagedExecutors {

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

    public static Function<Long, Optional<String>> threadNamer() {
        return id ->
            Optional.ofNullable(THREAD_NAMES.get(id));
    }

    static {
        THREAD_NAMES = new ConcurrentHashMap<>();
        DefaultLogFormatter.set(() -> new DefaultLogFormatter(threadNamer()));
    }

    private ManagedExecutors() {

    }

    @SuppressWarnings("StaticCollection")
    private static final ConcurrentMap<Long, String> THREAD_NAMES;

    private static final AtomicReference<Duration> KEEP_ALIVE_TIME =
        new AtomicReference<>(Duration.ofSeconds(10));

    private static final AtomicInteger CORE_THREADS_DEFAULT =
        new AtomicInteger(4);

    private static final AtomicInteger MAX_QUEUE_LENGTH_DEFAULT =
        new AtomicInteger(10);

    private static final LongAdder THREAD_COUNT = new LongAdder();

    private static ThreadFactory threadFactory(String prefix) {
        return runnable -> {
            String name = nextName(prefix);
            Thread thread = new Thread(
                () -> {
                    try {
                        runnable.run();
                    } catch (Throwable e) {
                        LoggerFactory.getLogger(ManagedExecutors.class)
                            .warn("{}: Uncaught exception {}", name, runnable, e);
                    } finally {
                        clearThreadName(Thread.currentThread());
                    }
                },
                name
            );
            return storedThreadName(thread, name);
        };
    }

    private static String nextName(String prefix) {
        try {
            return prefix + (prefix.endsWith("-") ? "" : "-") + THREAD_COUNT.longValue();
        } finally {
            THREAD_COUNT.increment();
        }
    }

    private static Thread storedThreadName(Thread thread, String name) {
        THREAD_NAMES.put(getId(thread), name);
        return thread;
    }

    private static void clearThreadName(Thread thread) {
        THREAD_NAMES.remove(getId(thread));
    }

    @SuppressWarnings("deprecation")
    private static long getId(Thread thread) {
        return thread.getId();
    }
}
