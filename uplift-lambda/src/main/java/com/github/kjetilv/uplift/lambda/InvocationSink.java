package com.github.kjetilv.uplift.lambda;

import module java.base;

@FunctionalInterface
public interface InvocationSink extends Consumer<Invocation> {

    @Override
    default void accept(Invocation invocation) {
        receive(invocation);
    }

    Invocation receive(Invocation invocation);
}
