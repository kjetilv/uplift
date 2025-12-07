package com.github.kjetilv.uplift.flogs;

import module java.base;

public interface LogFormatter<E> extends Function<E, String> {

    @Override
    default String apply(E entry) {
        return format(entry);
    }

    String format(E entry);
}
