package com.github.kjetilv.uplift.json.match;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class MapsStructure implements Structure<Object> {

    @Override
    public boolean isNull(Object object) {
        return object == null ||
               object.equals(Collections.emptyMap()) ||
               isArray(object) && arrayElements(object).findAny().isEmpty();
    }

    @Override
    public boolean isObject(Object object) {
        return object instanceof Map<?, ?>;
    }

    @Override
    public boolean isArray(Object array) {
        return array instanceof Iterable<?>;
    }

    @Override
    public Optional<Object> get(Object object, String field) {
        return object instanceof Map<?, ?> map
            ? Optional.ofNullable(map.get(field))
            : Optional.empty();
    }

    @Override
    public Stream<Object> arrayElements(Object array) {
        return array instanceof Iterable<?> iterable ? stream(iterable) : Stream.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> fieldsMap(Object object) {
        if (object instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new IllegalArgumentException("Not an object: " + object);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<Map.Entry<String, Object>> namedFields(Object object) {
        return object instanceof Map<?, ?> map ? ((Map<String, Object>) map).entrySet()
            .stream() : Stream.empty();
    }

    @Override
    public Object toObject(Map<String, ?> map) {
        return map;
    }

    @Override
    public Object combine(Object one, Object two) {
        return Combine.objects(one, two);
    }

    @SuppressWarnings("unchecked")
    private static Stream<Object> stream(Iterable<?> elements) {
        return (Stream<Object>) StreamSupport.stream(elements.spliterator(), false);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[]]";
    }
}
