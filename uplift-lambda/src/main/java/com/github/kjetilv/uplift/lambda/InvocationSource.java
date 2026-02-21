package com.github.kjetilv.uplift.lambda;

import module java.base;
import com.github.kjetilv.uplift.util.RuntimeCloseable;

@FunctionalInterface
public interface InvocationSource extends RuntimeCloseable {

    @Override
    default void close() {
    }

    Optional<CompletionStage<Invocation>> next();
}
