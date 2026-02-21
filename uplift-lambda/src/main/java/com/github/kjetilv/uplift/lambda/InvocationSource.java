package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.util.RuntimeCloseable;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface InvocationSource extends RuntimeCloseable {

    @Override
    default void close() {
    }

    Optional<CompletionStage<Invocation>> next();
}
