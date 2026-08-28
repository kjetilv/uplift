package com.github.kjetilv.uplift.json;

import module java.base;

import static java.util.Arrays.asList;

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

    @SuppressWarnings("unused")
    default FieldEvents boolArray(String field, List<Boolean> value) {
        return boolArray(field, value, id());
    }

    default FieldEvents stringArray(String field, String[] values) {
        return stringArray(field, values == null ? null : asList(values), id());
    }

    default FieldEvents stringArray(String field, List<String> values) {
        return stringArray(field, values, id());
    }

    default FieldEvents numberArray(String field, Number[] values) {
        return numberArray(field, values == null ? null : asList(values), id());
    }

    default FieldEvents numberArray(String field, long[] values) {
        return this.numberArray(field, values == null ? null : toNumbers(values), id());
    }

    default FieldEvents numberArray(String field, int[] values) {
        return this.numberArray(field, values == null ? null : toNumbers(values), id()
        );
    }

    default FieldEvents numberArray(String field, double[] values) {
        return this.numberArray(field, values == null ? null : toNumbers(values), id());
    }

    default FieldEvents numberArray(String field, float[] values) {
        return this.numberArray(field, values == null ? null : toNumbers(values), id());
    }

    default FieldEvents numberArray(String field, short[] values) {
        return this.numberArray(field, values == null ? null : toNumbers(values), id());
    }

    default FieldEvents numberArray(String field, byte[] values) {
        return this.numberArray(field, values == null ? null : toNumbers(values), id());
    }

    default <T> FieldEvents numberArray(String field, T[] values, Function<T, Number> toNumber) {
        return numberArray(field, values == null ? null : asList(values), toNumber);
    }

    default <T> FieldEvents boolArray(String field, T[] value, Function<T, Boolean> toBool) {
        return boolArray(field, value == null ? null : asList(value), toBool);
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

    private static List<Double> toNumbers(double[] values) {
        return Arrays.stream(values).boxed()
            .toList();
    }
}
