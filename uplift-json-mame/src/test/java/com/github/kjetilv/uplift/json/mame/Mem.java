package com.github.kjetilv.uplift.json.mame;

public record Mem(
    long freeM,
    long totalM,
    long maxM
) {

    public static Mem create() {
        Runtime runtime = Runtime.getRuntime();
        return new Mem(
            runtime.freeMemory() / M,
            runtime.totalMemory() / M,
            runtime.maxMemory() / M
        );
    }

    private static final int M = 1024 * 1024;
}
