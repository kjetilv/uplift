package com.github.kjetilv.uplift.json;

import java.util.List;
import java.util.function.Function;

@SuppressWarnings("unused")
public interface WriteEvents {

    <T extends Record> WriteEvents objectField(String field, T value, ObjectWriter<T> writer);

    <T extends Record> WriteEvents objectArrayField(String field, List<? extends T> values, ObjectWriter<T> writer);

    default WriteEvents stringField(String field, String value) {
        return stringField(field, value, Function.identity());
    }

    <T> WriteEvents stringField(String field, T value, Function<T, String> toString);

    default WriteEvents stringArrayField(String field, List<String> values) {
        return stringArrayField(field, values, Function.identity());
    }

    <T> WriteEvents stringArrayField(String field, List<T> values, Function<T, String> toString);

    default WriteEvents numberField(String field, Number value) {
        return numberField(field, value, Function.identity());
    }

    <T> WriteEvents numberField(String field, T value, Function<T, Number> toNumber);

    default WriteEvents numberArrayField(String field, List<? extends Number> values) {
        return numberArrayField(field, values, Function.identity());
    }

    <T> WriteEvents numberArrayField(String field, List<? extends T> values, Function<T, Number> toNumber);

    default WriteEvents boolField(String field, boolean value) {
        return boolField(field, value, Function.identity());
    }

    <T> WriteEvents boolField(String field, T value, Function<T, Boolean> toBool);

    default <T> WriteEvents boolArrayField(String field, List<Boolean> value) {
        return boolArrayField(field, value, Function.identity());
    }

    <T> WriteEvents boolArrayField(String field, List<? extends T> value, Function<T, Boolean> toBool);

    void done();
}
