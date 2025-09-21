package com.github.kjetilv.uplift.asynchttp;

import module java.base;

@FunctionalInterface
public interface BufferingWriter<B> extends Closeable {

    @Override
    default void close() {
    }

    void write(Writable<? extends B> writable);
}
