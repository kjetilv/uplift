package com.github.kjetilv.uplift.fq;

import com.github.kjetilv.uplift.util.RuntimeCloseable;

import java.io.Closeable;
import java.util.List;

public interface FqWriter<T> extends RuntimeCloseable {

    default void write(T item) {
        write(List.of(item));
    }

    void write(List<T> item);
}
