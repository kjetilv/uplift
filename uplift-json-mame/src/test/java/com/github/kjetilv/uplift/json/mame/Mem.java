package com.github.kjetilv.uplift.json.mame;

public record Mem(
    long usedM,
    long freeM,
    long totalM,
    long maxM
) {

    public static Mem create() {
        Runtime runtime = Runtime.getRuntime();
        long free = runtime.freeMemory();
        long total = runtime.totalMemory();
        return new Mem(
            (total - free) / M,
            free / M,
            total / M,
            runtime.maxMemory() / M
        );
    }

    private static final int M = 1024 * 1024;

    @Override
    public String toString() {
        return usedM + "M";
    }
}
