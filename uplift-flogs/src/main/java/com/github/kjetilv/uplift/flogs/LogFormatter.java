package com.github.kjetilv.uplift.flogs;

import java.util.function.Function;

interface LogFormatter<E> extends Function<E, String> {

    @Override
    default String apply(E entry) {
        return format(entry);
    }

    String format(E entry);
}
