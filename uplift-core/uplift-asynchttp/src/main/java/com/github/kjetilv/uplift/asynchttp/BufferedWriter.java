package com.github.kjetilv.uplift.asynchttp;

import java.io.Closeable;

@FunctionalInterface
public interface BufferedWriter<B> extends Closeable {

    @Override
    default void close() {
    }

    void write(Writable<? extends B> writable);
}
