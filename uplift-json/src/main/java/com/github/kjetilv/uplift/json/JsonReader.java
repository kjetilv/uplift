package com.github.kjetilv.uplift.json;

import module java.base;

public interface JsonReader<S, T extends Record> {

    T read(S source);

    void read(S source, Consumer<T> set);
}
