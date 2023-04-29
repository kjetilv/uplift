package com.github.kjetilv.uplift.kernel.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public final class Maps {

    public static <K, V> Map<K, V> nullSafe(Map<K, V> map) {
        return map == null || map.isEmpty() ? Collections.emptyMap() : map;
    }

    public static <K, V> Stream<V> get(Map<K, V> map, Predicate<K> keyLike) {
        return map.entrySet().stream()
            .filter(e -> keyLike.test(e.getKey()))
            .map(Map.Entry::getValue);
    }

    public static <K, V> Map<K, List<V>> groupBy(
        Collection<? extends V> items,
        Function<? super V, ? extends K> mapping
    ) {
        return Collections.unmodifiableMap(items.stream().collect(Collectors.groupingBy(
            mapping,
            LinkedHashMap::new,
            Collectors.toList()
        )));
    }

    public static <K, V> Map<K, V> indexBy(
        Collection<? extends V> items,
        Function<? super V, ? extends K> mapping
    ) {
        return Collections.unmodifiableMap(items.stream().collect(Collectors.toMap(
            mapping,
            Function.identity(),
            noCombine(),
            LinkedHashMap::new
        )));
    }

    public static <V, T, I extends Collection<V>> I noDuplicates(I l, Function<V, T> id) {
        if (l == null || l.size() <= 1) {
            return l;
        }
        List<Map.Entry<T, List<V>>> dupes = dupes(l, id);
        if (dupes.isEmpty()) {
            return l;
        }
        throw new IllegalStateException("Duplicates: " + dupes);
    }

    public static <K, V, T, C extends Collection<T>> Map<K, V> toMap(
        C c,
        Function<? super T, ? extends K> key,
        Function<? super T, ? extends V> val
    ) {
        if (c == null) {
            return Collections.emptyMap();
        }
        return c.stream().collect(Collectors.toMap(
            key,
            val,
            noCombine(),
            LinkedHashMap::new
        ));
    }

    private static <V> BinaryOperator<V> noCombine() {
        return (v1, v2) -> {
            throw new IllegalStateException("No combine: " + v1 + " / " + v2);
        };
    }

    public static <K, V> Map.Entry<K, Optional<? extends V>> ent(K key, V value) {
        return Map.entry(key, Optional.ofNullable(value));
    }

    @SafeVarargs
    public static Map<?, ?> toMap(Map.Entry<?, Optional<?>>... entries) {
        return Stream.of(entries)
            .filter(entry -> entry.getValue().isPresent())
            .collect(toMap());
    }

    private Maps() {
    }

    private static <V, T, I extends Collection<V>> List<Map.Entry<T, List<V>>> dupes(
        I l,
        Function<? super V, ? extends T> id
    ) {
        Map<T, List<V>> map = groupBy(l, id);
        return map.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .toList();
    }

    private static <K, V> Collector<Map.Entry<? extends K, ? extends V>, ?, ? extends Map<K, V>> toMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> {
            throw new IllegalStateException("No combine");
        }, LinkedHashMap::new);
    }
}
