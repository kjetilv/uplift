package com.github.kjetilv.uplift.json.match;

import java.util.Objects;

public record Diff<T>(T expected, T found) {

    boolean isDiff() {
        return !Objects.equals(expected, found);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + (isDiff() ? expected + " != " + found : expected) + "]";
    }
}
