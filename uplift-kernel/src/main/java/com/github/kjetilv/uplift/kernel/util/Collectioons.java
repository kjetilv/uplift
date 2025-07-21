package com.github.kjetilv.uplift.kernel.util;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Some collection-related nitty-gritty bits.
 */
public final class Collectioons {

    public static <T, R> List<R> transform(List<? extends T> list, Function<T, R> transform) {
        return list.stream()
            .map(transform)
            .toList();
    }

    public static <T, R> List<R> transform(Iterable<? extends T> list, Function<T, R> transform) {
        return stream(list)
            .map(transform)
            .toList();
    }

    public static boolean isEmptyArray(Object array) {
        return array.getClass().isArray() && Array.getLength(array) == 0;
    }

    public static Iterable<?> iterable(Object array) {
        return () -> new Iterator<>() {

            private final int length = Array.getLength(array);

            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < length;
            }

            @Override
            public Object next() {
                try {
                    return Array.get(array, index);
                } finally {
                    index++;
                }
            }
        };
    }

    public static <T> Stream<T> stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    private Collectioons() {
    }
}
