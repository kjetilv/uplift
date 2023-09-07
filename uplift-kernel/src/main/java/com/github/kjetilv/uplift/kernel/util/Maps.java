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

    public static <K, V> Map.Entry<K, Optional<? extends V>> optEntry(K key, V value) {
        return Map.entry(key, Optional.ofNullable(value));
    }

    @SafeVarargs
    public static <K, V> Map<K, V> fromOptEntries(Map.Entry<K, Optional<? extends V>>... entries) {
        return realEntries(entries).collect(toLinkedHashMap(entries.length));
    }

    public static <K, V> Map<K, V> fromOptEntries(List<Map.Entry<K, Optional<? extends V>>> entryList) {
        return realEntries(entryList.stream()).collect(toLinkedHashMap(entryList.size()));
    }

    public static <K, V> Map<K, V> fromOptEntries(Stream<Map.Entry<K, Optional<? extends V>>> entryStream) {
        return realEntries(entryStream).collect(toLinkedHashMap());
    }

    public static <K, V> Map<K, V> fromEntries(List<Map.Entry<K, V>> entryList) {
        return entryList.stream().collect(toLinkedHashMap(entryList.size()));
    }

    public static <K, V> Map<K, V> fromEntries(Stream<Map.Entry<K, V>> entryStream) {
        return entryStream.collect(toLinkedHashMap());
    }

    private Maps() {
    }

    private static <K, V> Stream<Map.Entry<K, V>> realEntries(Map.Entry<K, Optional<? extends V>>[] entries) {
        return realEntries(Stream.of(entries));
    }

    private static <K, V> Stream<Map.Entry<K, V>> realEntries(Stream<Map.Entry<K, Optional<? extends V>>> entries) {
        return entries
            .filter(entry -> entry.getValue().isPresent())
            .map(entry -> Map.entry(entry.getKey(), entry.getValue().get()));
    }

    private static <V> BinaryOperator<V> noCombine() {
        return (v1, v2) -> {
            throw new IllegalStateException("No combine: " + v1 + " / " + v2);
        };
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

    private static <K, V> Collector<Map.Entry<? extends K, ? extends V>, ?, ? extends Map<K, V>> toLinkedHashMap() {
        return toLinkedHashMap(0);
    }

    private static <K, V> Collector<Map.Entry<? extends K, ? extends V>, ?, ? extends Map<K, V>> toLinkedHashMap(
        int size
    ) {
        return Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (v1, v2) -> {
                throw new IllegalStateException("No combine: " + v1 + "/" + v2);
            },
            () ->
                size > 0 ? new LinkedHashMap<>(size * 2) : new LinkedHashMap<>()
        );
    }
}
