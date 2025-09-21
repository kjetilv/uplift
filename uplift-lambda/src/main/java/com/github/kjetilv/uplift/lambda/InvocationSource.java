package com.github.kjetilv.uplift.lambda;

import module java.base;

@FunctionalInterface
public interface InvocationSource<Q, R> extends Closeable {

    @Override
    default void close() {
    }

    Optional<CompletionStage<Invocation<Q, R>>> next();
}
