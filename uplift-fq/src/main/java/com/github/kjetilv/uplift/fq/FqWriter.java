package com.github.kjetilv.uplift.fq;

import java.io.Closeable;
import java.util.List;

public interface FqWriter<T> extends Closeable {

    default void write(T item) {
        write(List.of(item));
    }

    void write(List<T> item);

    @Override
    void close();
}
