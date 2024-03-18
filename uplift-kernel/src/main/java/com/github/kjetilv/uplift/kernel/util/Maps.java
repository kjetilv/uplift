package com.github.kjetilv.uplift.kernel.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private Maps() {
    }

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
}
