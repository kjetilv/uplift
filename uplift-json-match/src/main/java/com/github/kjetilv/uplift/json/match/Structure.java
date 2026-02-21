package com.github.kjetilv.uplift.json.match;

import module java.base;

@SuppressWarnings("unused")
public interface Structure<T> {

    default List<T> listElements(T array) {
        return arrayElements(array).toList();
    }

    default Map<String, T> fieldsMap(T object) {
        return namedFields(object).collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue
        ));
    }

    default <R> Stream<R> mapNamedFields(T object, BiFunction<String, T, Stream<R>> map) {
        return namedFields(object).flatMap(e ->
            map.apply(e.getKey(), e.getValue()));
    }

    default <R> Stream<R> mapArrayElements(T array, Function<T, Stream<R>> map) {
        return arrayElements(array).flatMap(map);
    }

    boolean isNull(T object);

    boolean isObject(T object);

    boolean isArray(T array);

    Optional<T> get(T object, String field);

    Optional<T> get(T array, int index);

    Stream<T> arrayElements(T array);

    boolean isEmpty(T array);

    Stream<Map.Entry<String, T>> namedFields(T object);

    T toObject(Map<String, ?> map);

    T combine(T one, T two);
}
