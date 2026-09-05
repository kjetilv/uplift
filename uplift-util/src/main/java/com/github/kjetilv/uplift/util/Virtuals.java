package com.github.kjetilv.uplift.util;

import module java.base;

public final class Virtuals {

    public static ExecutorService executor(String name) {
        return Executors.newThreadPerTaskExecutor(virtualThreadFactory(name));
    }

    public static ThreadFactory virtualThreadFactory(String name) {
        LongAdder threadNo = new LongAdder();
        return r -> {
            try {
                return thread(name + "-" + threadNo.longValue(), r);
            } finally {
                threadNo.increment();
            }
        };
    }

    public static Thread.Builder.OfVirtual thread(String threadName) {
        return Thread.ofVirtual().name(threadName);
    }

    public static Thread thread(String threadName, Runnable runnable) {
        return thread(threadName).unstarted(runnable);
    }

    private Virtuals() {
    }
}
