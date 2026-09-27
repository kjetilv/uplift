package com.github.kjetilv.uplift.fq;

import module java.base;
import com.github.kjetilv.uplift.util.RuntimeCloseable;

public interface FqWriter<T> extends RuntimeCloseable {

    default void write(T item) {
        write(List.of(item));
    }

    void write(List<T> item);
}
