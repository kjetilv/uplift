package com.github.kjetilv.uplift.util;

import module java.base;

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
        return c == null ? Collections.emptyMap() : c.stream()
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
        return map == null ? Collections.emptyMap() : map.entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> tv.apply(e.getValue())
            ));
    }

    public static <K, V> Supplier<Map<K, V>> getSizedMap(int size) {
        return () -> sizedMap(size);
    }

    public static <K, V> Map<K, V> sizedMap(Map<K, V> input) {
        Map<K, V> sizedMap = sizedMap(input.size());
        sizedMap.putAll(input);
        return Collections.unmodifiableMap(sizedMap);
    }

    public static <K, V, R> Map<K, R> transformMap(
        Map<K, V> map,
        Function<V, R> transform
    ) {
        return Collections.unmodifiableMap(map.entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> transform.apply(entry.getValue()),
                noMerge(),
                getSizedMap(map.size())
            )));
    }

    public static <R> BinaryOperator<R> noMerge() {
        return (r1, r2) -> {
            throw new IllegalStateException("Duplicate key " + r1 + "/" + r2);
        };
    }

    static <K, T, V, R> Map<K, R> transformMap(
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
                getSizedMap(map.size())
            )));
    }

    private Maps() {
    }

    private static final int MAX_POWER_OF_TWO = 1 << Integer.SIZE - 2;

    private static <K, V> Map<K, V> sizedMap(int size) {
        return new HashMap<>(capacity(size));
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

    private static int capacity(int expectedSize) {
        return expectedSize < 3 ? expectedSize + 1
            : expectedSize < MAX_POWER_OF_TWO ? (int) ceil(expectedSize / 0.75)
                : Integer.MAX_VALUE;
    }
}
