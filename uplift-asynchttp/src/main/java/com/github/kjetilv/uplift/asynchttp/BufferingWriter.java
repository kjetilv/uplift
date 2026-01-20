package com.github.kjetilv.uplift.asynchttp;

import module java.base;
import com.github.kjetilv.uplift.util.RuntimeCloseable;

@FunctionalInterface
public interface BufferingWriter<B> extends RuntimeCloseable {

    @Override
    default void close() {
    }

    void write(Writable<? extends B> writable);
}
