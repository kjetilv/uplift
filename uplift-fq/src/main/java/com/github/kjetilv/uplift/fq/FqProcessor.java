package com.github.kjetilv.uplift.fq;

import java.util.List;

public interface FqProcessor<T> {

    default T process(T t) {
        return process(List.of(t)).getFirst();
    }

    List<T> process(List<T> items);
}
