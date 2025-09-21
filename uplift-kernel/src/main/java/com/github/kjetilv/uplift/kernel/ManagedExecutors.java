package com.github.kjetilv.uplift.kernel;

import module java.base;
import module uplift.flogs;

public final class ManagedExecutors {

    public static ExecutorService executor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    private ManagedExecutors() {
    }

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
