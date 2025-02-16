package com.github.kjetilv.uplift.json.trie;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

interface IntMap<T> extends IntFunction<T> {

    @SuppressWarnings("unchecked")
    static <T> IntMap<T> from(Map<? extends Number, T> map) {
        int size = map.size();
        if (size == 0) {
            return (IntMap<T>) None.NONE;
        }
        if (size == 1) {
            return new One<>(singleKey(map), map.values().iterator().next());
        }
        List<? extends Map.Entry<? extends Number, T>> list = map.entrySet()
            .stream()
            .sorted(Comparator.comparing(IntMap::intKey))
            .toList();

        int[] keys = list
            .stream()
            .mapToInt(IntMap::intKey)
            .toArray();
        Object[] values = list.stream()
            .map(Map.Entry::getValue).toArray();

        if (size == 2) {
            return new Two<>(keys[0], (T) values[0], keys[1], (T) values[1]);
        }

        return new Sparse<>(keys, values);
    }

    private static <T> int intKey(Map.Entry<? extends Number, T> e) {
        return e.getKey().intValue();
    }

    private static <T> int singleKey(Map<? extends Number, T> map) {
        return map.keySet().iterator().next().intValue();
    }

    record Sparse<T>(int[] keys, Object[] values) implements IntMap<T> {

        @SuppressWarnings("unchecked")
        @Override
        public T apply(int value) {
            int i = Binary.search(keys, value);
            return i < 0 ? null : (T) values[i];
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" +
                   IntStream.range(0, keys.length)
                       .mapToObj(i -> keys[i] + "=" + values[i])
                       .collect(Collectors.joining(" ")) +
                   "]";
        }
    }

    record None<T>() implements IntMap<T> {

        @Override
        public T apply(int i) {
            return null;
        }

        private static final None<?> NONE = new None<>();

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[]";
        }
    }

    record One<T>(int key, T value) implements IntMap<T> {

        @Override
        public T apply(int i) {
            return i == key ? value : null;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + key + "=" + value + "]";
        }
    }

    record Two<T>(int key1, T value1, int key2, T value2) implements IntMap<T> {

        @Override
        public T apply(int value) {
            return value == key1 ? value1
                : value == key2 ? value2
                    : null;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" +
                   key1 + "=" + value1 + " " +
                   key2 + "=" + value2 +
                   "]";
        }
    }
}