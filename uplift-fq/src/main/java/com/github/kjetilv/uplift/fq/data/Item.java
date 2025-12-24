package com.github.kjetilv.uplift.fq.data;

import java.util.function.Function;

public record Item<T>(long serial, T value) implements Entry<T> {

    @Override
    public Entry<T> map(Function<T, T> transform) {
        try {
            return new Item<>(serial, transform.apply(value));
        } catch (Exception e) {
            return new Failure<>(serial, e);
        }
    }
}
