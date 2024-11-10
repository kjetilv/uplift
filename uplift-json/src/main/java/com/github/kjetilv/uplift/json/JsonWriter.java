package com.github.kjetilv.uplift.json;

public interface JsonWriter<S, T extends Record, O> {

    S write(T t);

    void write(T t, O out);
}
