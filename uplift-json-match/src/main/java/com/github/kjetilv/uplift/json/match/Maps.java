package com.github.kjetilv.uplift.json.match;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Maps {

    static <K, V> Map<K, V> toMap(Stream<Map.Entry<K, V>> entryStream) {
        return toMap(entryStream, false);
    }

    static <K, V> Map<K, V> toMap(Stream<Map.Entry<K, V>> entryStream, boolean failOnMerge) {
        return toMap(entryStream, null, failOnMerge);
    }

    static <K, V> Map<K, V> toMap(Stream<Map.Entry<K, V>> entryStream, BinaryOperator<V> mergeFunction) {
        return toMap(entryStream, mergeFunction, false);
    }

    static <K, V> Map<K, V> toMap(
        Stream<Map.Entry<K, V>> entryStream,
        BinaryOperator<V> mergeFunction,
        boolean failOnMerge
    ) {
        return entryStream.collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            mergeFunction != null ? mergeFunction
                : failOnMerge ? Maps::noCombine
                    : Maps::combineSame,
            LinkedHashMap::new
        ));
    }

    private Maps() {

    }

    private static <V> V noCombine(V o1, V o2) {
        throw new IllegalStateException("Cannot combine: " + o1 + " / " + o2);
    }

    private static <V> V combineSame(V o1, V o2) {
        if (o1.equals(o2)) {
            return o1;
        }
        throw new IllegalStateException("Cannot combine: " + o1 + " / " + o2);
    }
}
