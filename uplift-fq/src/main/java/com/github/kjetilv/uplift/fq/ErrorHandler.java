package com.github.kjetilv.uplift.fq;

import java.util.List;

public interface ErrorHandler<T> {

    default void failed(Flow<T> flow, T item, Exception e) {
        failed(flow, List.of(item), e);
    }

    void failed(Flow<T> flow, List<T> items, Exception e);
}
