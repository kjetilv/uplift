package com.github.kjetilv.uplift.fq.flows;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.LongSupplier;

final class DefaultRun implements FqFlows.Run {

    private final CompletableFuture<Void> flowRun;

    private final CompletableFuture<Long> feedItems;

    DefaultRun(CompletableFuture<Void> runFlows, LongSupplier feedItems) {
        this.flowRun = runFlows;
        this.feedItems = CompletableFuture.supplyAsync(feedItems::getAsLong, EXECUTOR);
    }

    @Override
    public long count() {
        return feedItems.join();
    }

    @Override
    public FqFlows.Run join() {
        flowRun.join();
        return this;
    }

    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
}
