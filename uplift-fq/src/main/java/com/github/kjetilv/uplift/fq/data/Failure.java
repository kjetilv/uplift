package com.github.kjetilv.uplift.fq.data;

import java.util.function.Function;

public record Failure<T>(long serial, Exception error) implements Entry<T> {

    @Override
    public Entry<T> map(Function<T, T> transform) {
        return this;
    }
}
