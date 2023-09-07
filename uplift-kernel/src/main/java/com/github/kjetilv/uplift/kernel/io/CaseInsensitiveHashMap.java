package com.github.kjetilv.uplift.kernel.io;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public final class CaseInsensitiveHashMap<V> implements Map<String, V> {

    public static <T, V> Collector<T, ?, Map<String, List<V>>> caseInsensitive(
        Function<? super T, String> key, Function<? super T, ? extends List<V>> values
    ) {
        return caseInsensitive(key, values, (vs1, vs2) -> Stream.concat(vs1.stream(), vs2.stream()).toList());
    }

    public static <T, V> Collector<T, ?, Map<String, V>> caseInsensitive(
        Function<? super T, String> key,
        Function<? super T, ? extends V> values,
        BiFunction<? super V, ? super V, ? extends V> merge
    ) {
        return Collectors.toMap(key, values, merge::apply, CaseInsensitiveHashMap::new);
    }

    public static <T> Map<String, T> wrap(Map<String, T> map) {
        return map == null || map.isEmpty()
            ? Collections.emptyMap()
            : map instanceof CaseInsensitiveHashMap<T> ? map : new CaseInsensitiveHashMap<>(map);
    }

    private final Map<String, V> map;

    public CaseInsensitiveHashMap() {
        this(null);
    }

    @SuppressWarnings("WeakerAccess")
    public CaseInsensitiveHashMap(Map<String, V> map) {
        this.map = map == null
            ? new LinkedHashMap<>()
            : new LinkedHashMap<>(requireNonNull(map, "map"));
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(lc(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(lc(key));
    }

    @Override
    public V put(String key, V value) {
        return map.put(lc(key), value);
    }

    @Override
    public V remove(Object key) {
        return map.remove(lc(key));
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        return map.entrySet();
    }

    private static String lc(Object key) {
        return requireNonNull(key, "key").toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
