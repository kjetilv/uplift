package com.github.kjetilv.uplift.fq.flows;

import module java.base;

import static java.util.Objects.requireNonNull;

final class DefaultRun<T> implements FqFlows.Run<T> {

    private final CompletableFuture<List<FlowRun<T>>> runFlows;

    private final CompletableFuture<Long> feedItems;

    DefaultRun(CompletableFuture<List<FlowRun<T>>> runFlows, LongSupplier feedItems) {
        this.runFlows = requireNonNull(runFlows, "runFlows");
        this.feedItems = CompletableFuture.supplyAsync(
            requireNonNull(feedItems, "feedItems")::getAsLong,
            Executors.newVirtualThreadPerTaskExecutor()
        );
    }

    @Override
    public long count() {
        return feedItems.join();
    }

    @Override
    public List<FlowRun<T>> join() {
        return runFlows.join();
    }
}
