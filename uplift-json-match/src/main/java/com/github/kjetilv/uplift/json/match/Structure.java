package com.github.kjetilv.uplift.json.match;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public interface Structure<T> {

    boolean isNull(T object);

    boolean isObject(T object);

    boolean isArray(T array);

    Optional<T> get(T object, String field);

    Stream<T> arrayElements(T array);

    default List<T> listElements(T array) {
        return arrayElements(array).toList();
    }

    default Map<String, T> fieldsMap(T object) {
        return namedFields(object).collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue
        ));
    }

    Stream<Map.Entry<String, T>> namedFields(T object);

    T toObject(Map<String, ?> map);

    T combine(T one, T two);

    default <R> Stream<R> mapNamedFields(T object, BiFunction<String, T, Stream<R>> map) {
        return namedFields(object).flatMap(e ->
            map.apply(e.getKey(), e.getValue()));
    }

    default <R> Stream<R> mapArrayElements(T array, Function<T, Stream<R>> map) {
        return arrayElements(array).flatMap(map);
    }
}
