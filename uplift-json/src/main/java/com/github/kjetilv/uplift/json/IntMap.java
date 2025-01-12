package com.github.kjetilv.uplift.json;

import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

interface IntMap<T> extends IntFunction<T> {

    @SuppressWarnings("unchecked")
    static <T> IntMap<T> from(Map<Byte, T> map) {
        int size = map.size();
        if (size == 0) {
            return new None<>();
        }
        if (size == 1) {
            return new Single<>(map.keySet().iterator().next(), map.values().iterator().next());
        }
        List<Map.Entry<Byte, T>> list = map.entrySet()
            .stream().sorted(Map.Entry.comparingByKey())
            .toList();

        int[] keys = list.stream().mapToInt(entry -> entry.getKey().intValue()).toArray();
        Object[] values = list.stream().map(Map.Entry::getValue).toArray();

        if (size == 2) {
            return new Two<>(keys[0], (T)values[0], keys[1], (T)values[1]);
        }

        return new Sparse<>(keys, values);
    }

    record Single<T>(int key, T value) implements IntMap<T> {

        @Override
        public T apply(int i) {
            return i == key ? value : null;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + key + "=" + value + "]";
        }
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

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[]";
        }
    }

    record Two<T>(int key1, T value1, int key2, T value2) implements IntMap<T> {

        @Override
        public T apply(int value) {
            return value == key1 ? value1 : value == key2 ? value2 : null;
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