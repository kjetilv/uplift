package com.github.kjetilv.uplift.json;

@FunctionalInterface
public interface JsonWriter<S, T extends Record, O> {

    default S write(T t) {
        throw new IllegalStateException(this + " does not support returned values");
    }

    void write(T t, O out);
}
