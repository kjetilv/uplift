package com.github.kjetilv.uplift.lambda;

import java.util.function.Consumer;

@FunctionalInterface
public interface InvocationSink<Q, R> extends Consumer<Invocation<Q, R>> {

    @Override
    default void accept(Invocation<Q, R> invocation) {
        receive(invocation);
    }

    Invocation<Q, R> receive(Invocation<Q, R> invocation);
}
