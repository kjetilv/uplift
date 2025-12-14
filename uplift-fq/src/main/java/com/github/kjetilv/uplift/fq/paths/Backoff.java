package com.github.kjetilv.uplift.fq.paths;

final class Backoff {

    private long time = MIN_SLEEP;

    void zzz() {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted", e);
        }
        time = Math.min(time * 2, MAX_SLEEP);
    }

    private static final long MAX_SLEEP = 100L;

    private static final long MIN_SLEEP = 1L;
}
