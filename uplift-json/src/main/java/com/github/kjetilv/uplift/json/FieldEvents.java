package com.github.kjetilv.uplift.json;

import module java.base;

public interface FieldEvents {

    default FieldEvents string(String field, String value) {
        return string(field, value, Function.identity());
    }

    default FieldEvents stringArray(String field, List<String> values) {
        return stringArray(field, values, Function.identity());
    }

    default FieldEvents number(String field, Number value) {
        return number(field, value, Function.identity());
    }

    default FieldEvents numberArray(String field, List<? extends Number> values) {
        return numberArray(field, values, Function.identity());
    }

    default FieldEvents bool(String field, boolean value) {
        return bool(field, value, Function.identity());
    }

    @SuppressWarnings("unused")
    default FieldEvents boolArray(String field, List<Boolean> value) {
        return boolArray(field, value, Function.identity());
    }

    FieldEvents map(String field, Map<?, ?> value, ObjectWriter<Map<?, ?>> writer);

    <T extends Record> FieldEvents object(String field, T value, ObjectWriter<T> writer);

    <T extends Record> FieldEvents objectArray(String field, List<? extends T> values, ObjectWriter<T> writer);

    <T> FieldEvents string(String field, T value, Function<T, String> toString);

    <T> FieldEvents stringArray(String field, List<T> values, Function<T, String> toString);

    <T> FieldEvents number(String field, T value, Function<T, Number> toNumber);

    <T> FieldEvents numberArray(String field, List<? extends T> values, Function<T, Number> toNumber);

    <T> FieldEvents bool(String field, T value, Function<T, Boolean> toBool);

    <T> FieldEvents boolArray(String field, List<? extends T> value, Function<T, Boolean> toBool);

    void done();
}
