package com.github.kjetilv.uplift.fq.paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public record Backoff(String description, long time, Object lock, Instant starting) {

    private static final Logger log = LoggerFactory.getLogger(Backoff.class);

    public static Backoff sleepAndUpdate(String description, Backoff backoff) {
        return backoff == null
            ? new Backoff(description)
            : backoff.zzz(description);
    }

    private Backoff(String description) {
        this(description, MIN_SLEEP, new boolean[0], Instant.now());
    }

    private Backoff zzz(String description) {
        synchronized (lock) {
            try {
                lock.wait(time);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted", e);
            }
        }
        if (time == MAX_SLEEP) {
            return this;
        }
        var minTime = Math.min(time * 2, MAX_SLEEP);
        if (minTime == MAX_SLEEP) {
            log.warn("Reached max sleep time {}ms: {}", minTime, description);
        }
        return new Backoff(description, minTime, lock, starting);
    }

    private static final long MAX_SLEEP = 100L;

    private static final long MIN_SLEEP = 1L;
}
