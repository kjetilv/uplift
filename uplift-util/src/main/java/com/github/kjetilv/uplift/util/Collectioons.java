package com.github.kjetilv.uplift.util;

import module java.base;

/// Some collection-related nitty-gritty bits.
public final class Collectioons {

    public static <T, L extends Collection<T>, R> List<R> transform(
        L list,
        Function<T, R> transform
    ) {
        return list.stream()
            .map(transform)
            .toList();
    }

    public static <T, R> List<R> transform(
        Iterable<? extends T> list,
        Function<T, R> transform
    ) {
        Stream<? extends T> stream = stream(list);
        return stream
            .map(transform)
            .toList();
    }

    public static boolean isEmptyArray(Object array) {
        return array.getClass().isArray() && Array.getLength(array) == 0;
    }

    public static Iterable<?> iterable(Object array) {
        return () -> new Iterator<>() {

            private final int length = Array.getLength(array);

            private int index;

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

    private Collectioons() {
    }

    private static <T> Stream<T> stream(Iterable<T> iterable) {
        return iterable instanceof Collection<T> collection
            ? collection.stream()
            : StreamSupport.stream(iterable.spliterator(), false);
    }
}
