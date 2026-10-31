package com.github.kjetilv.uplift.json;

import module java.base;

@SuppressWarnings("unused") // Used from generated code
public interface FieldEvents {

    FieldEvents string(String field, String value);

    FieldEvents number(String field, Number value);

    FieldEvents bool(String field, Boolean value);

    FieldEvents numberArray(String field, List<? extends Number> values);

    FieldEvents boolArray(String field, List<Boolean> values);

    FieldEvents numberArray(String field, double[] values);

    FieldEvents numberArray(String field, short[] values);

    FieldEvents numberArray(String field, Number[] values);

    <T> FieldEvents numberArray(String field, T[] values, Function<T, Number> toNumber);

    <T> FieldEvents boolArray(String field, T[] value, Function<T, Boolean> toBool);

    FieldEvents numberArray(String field, long[] values);

    FieldEvents numberArray(String field, int[] values);

    FieldEvents numberArray(String field, float[] values);

    FieldEvents numberArray(String field, byte[] values);

    FieldEvents boolArray(String field, boolean[] value);

    FieldEvents stringArray(String field, String[] values);

    default FieldEvents stringArray(String field, char[] values) {
        List<String> strings = values == null ? null
            : IntStream.range(0, values.length)
                .mapToObj(i -> String.valueOf(values[i]))
                .toList();
        return stringArray(field, strings);
    }

    default <T> FieldEvents stringArray(String field, T[] values, Function<T, String> toString) {
        return stringArray(field, values == null ? null : Arrays.asList(values), toString);
    }

    FieldEvents stringArray(String field, List<String> values);

    <T extends Record> FieldEvents objectArray(String field, T[] values, ObjectWriter<T> writer);

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
