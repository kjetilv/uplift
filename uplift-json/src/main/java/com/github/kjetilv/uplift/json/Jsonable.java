package com.github.kjetilv.uplift.json;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface Jsonable<T> {

    Function<Object, T> reader();

    BiConsumer<T, Sink> writer();
}
