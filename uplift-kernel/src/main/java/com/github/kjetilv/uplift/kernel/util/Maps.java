package com.github.kjetilv.uplift.kernel.util;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.ceil;

@SuppressWarnings("unused")
public final class Maps {

    public static <K, V> Stream<V> get(Map<K, V> map, Predicate<K> keyLike) {
        return map.entrySet()
            .stream()
            .filter(e -> keyLike.test(e.getKey()))
            .map(Map.Entry::getValue);
    }

    public static <K, V> Map<K, V> indexBy(
        Collection<? extends V> items,
        Function<? super V, ? extends K> mapping
    ) {
        return Collections.unmodifiableMap(items.stream()
            .collect(Collectors.toMap(
                mapping,
                Function.identity(),
                noCombine(),
                LinkedHashMap::new
            )));
    }

    public static <K, V, T, C extends Collection<T>> Map<K, V> toMap(
        C c,
        Function<? super T, ? extends K> key,
        Function<? super T, ? extends V> val
    ) {
        if (c == null) {
            return Collections.emptyMap();
        }
        return c.stream()
            .collect(Collectors.toMap(
                key,
                val,
                noCombine(),
                LinkedHashMap::new
            ));
    }

    public static <K, V> Map<K, V> fromEntries(Stream<Map.Entry<K, V>> entryStream) {
        return entryStream.collect(toLinkedHashMap());
    }

    public static <K, T, V> Map<K, V> mapValues(Map<K, T> map, Function<T, V> tv) {
        if (map == null) {
            return Collections.emptyMap();
        }
        return map.entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> tv.apply(e.getValue())
            ));
    }

    public static <K, V> Supplier<Map<K, V>> sizedMap(int size) {
        return () -> new HashMap<>(capacity(size));
    }

    public static <K, V, R> Map<K, R> transformValues(
        Map<K, V> map,
        Function<V, R> transform
    ) {
        return Collections.unmodifiableMap(map.entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> transform.apply(entry.getValue()),
                noMerge(),
                sizedMap(map.size())
            )));
    }

    static <K, T, V, R> Map<K, R> transformValues(
        Map<T, V> map,
        Function<T, K> keyNormalizer,
        Function<V, R> transform
    ) {
        return Collections.unmodifiableMap(map.entrySet()
            .stream()
            .collect(Collectors.toMap(
                entry -> keyNormalizer.apply(entry.getKey()),
                entry -> transform.apply(entry.getValue()),
                noMerge(),
                sizedMap(map.size())
            )));
    }

    public static <R> BinaryOperator<R> noMerge() {
        return (r1, r2) -> {
            throw new IllegalStateException("Duplicate key " + r1 + "/" + r2);
        };
    }

    private Maps() {
    }

    private static final int MAX_POWER_OF_TWO = 1 << Integer.SIZE - 2;

    private static <V> BinaryOperator<V> noCombine() {
        return (v1, v2) -> {
            throw new IllegalStateException("No combine: " + v1 + " / " + v2);
        };
    }

    private static <K, V> Collector<Map.Entry<? extends K, ? extends V>, ?, ? extends Map<K, V>> toLinkedHashMap() {
        return Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (v1, v2) -> {
                throw new IllegalStateException("No combine: " + v1 + "/" + v2);
            },
            LinkedHashMap::new
        );
    }

    private static int capacity(int expectedSize) {
        return expectedSize < 3 ? expectedSize + 1
            : expectedSize < MAX_POWER_OF_TWO ? (int) ceil(expectedSize / 0.75)
                : Integer.MAX_VALUE;
    }
}
