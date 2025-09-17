package com.github.kjetilv.uplift.edam;

import java.util.function.ToLongFunction;

public interface HashFun<T> extends ToLongFunction<T> {

    @Override
    default long applyAsLong(T value) {
        return compute(value);
    }

    long compute(T value);

    default int sizeMultiple() {
        return DEFAULT_SIZE_MULTIPLE;
    }

    default long slotCount(long itemCount) {
        if (itemCount > 0) {
            return sizeMultiple() * itemCount;
        }
        throw new IllegalArgumentException("count should be > 0: " + itemCount);
    }

    int DEFAULT_SIZE_MULTIPLE = 1_000;
}
