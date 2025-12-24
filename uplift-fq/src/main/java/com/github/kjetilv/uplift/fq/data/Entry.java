package com.github.kjetilv.uplift.fq.data;

import java.util.function.Function;

public sealed interface Entry<T> permits Item, Failure {

    long serial();

    Entry<T> map(Function<T, T> transform);
}
