package com.github.kjetilv.uplift.json;

import module java.base;

@SuppressWarnings("unused") // Used from generated code
public interface FieldEvents {

    default FieldEvents string(String field, String value) {
        return string(field, value, id());
    }

    default FieldEvents number(String field, Number value) {
        return number(field, value, id());
    }

    default FieldEvents bool(String field, Boolean value) {
        return bool(field, value, id());
    }

    default FieldEvents numberArray(String field, List<? extends Number> values) {
        return numberArray(field, values, id());
    }

    default FieldEvents boolArray(String field, List<Boolean> value) {
        return boolArray(field, value, id());
    }

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

    private static List<Integer> toNumbers(int[] values) {
        return Arrays.stream(values).boxed()
            .toList();
    }

    private static List<Long> toNumbers(long[] values) {
        return Arrays.stream(values).boxed()
            .toList();
    }

    private static <T> Function<T, T> id() {
        return Function.identity();
    }

    private static Number[] toNumbers(float[] values) {
        Number[] numbers = new Number[values.length];
        for (var i = 0; i < values.length; i++) {
            numbers[i] = values[i];
        }
        return numbers;
    }

    private static Number[] toNumbers(short[] values) {
        Number[] numbers = new Number[values.length];
        for (var i = 0; i < values.length; i++) {
            numbers[i] = values[i];
        }
        return numbers;
    }

    private static Number[] toNumbers(byte[] values) {
        Number[] numbers = new Number[values.length];
        for (var i = 0; i < values.length; i++) {
            numbers[i] = values[i];
        }
        return numbers;
    }

    private static Boolean[] toBooleans(boolean[] values) {
        Boolean[] booleans = new Boolean[values.length];
        for (var i = 0; i < values.length; i++) {
            booleans[i] = values[i];
        }
        return booleans;
    }

    private static List<Double> toNumbers(double[] values) {
        return Arrays.stream(values).boxed()
            .toList();
    }
}
