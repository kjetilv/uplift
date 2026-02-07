package com.github.kjetilv.uplift.util;

import java.util.Optional;

@SuppressWarnings({"unchecked", "OptionalUsedAsFieldOrParameterType"})
public sealed interface Maybe<T> {

    Nothing<?> NOTHING = new Nothing<>();

    static <T> Maybe<T> a(Optional<T> optional) {
        return optional
            .<Maybe<T>>map(A::new)
            .orElseGet(() -> (Nothing<T>) NOTHING);
    }

    record A<T>(T value) implements Maybe<T> {
    }

    record Nothing<T>() implements Maybe<T> {
    }
}
