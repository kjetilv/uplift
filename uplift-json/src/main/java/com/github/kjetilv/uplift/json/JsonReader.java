package com.github.kjetilv.uplift.json;

import java.util.function.Consumer;

public interface JsonReader<S, T extends Record> {

    T read(S source);

    void read(S source, Consumer<T> set);
}
