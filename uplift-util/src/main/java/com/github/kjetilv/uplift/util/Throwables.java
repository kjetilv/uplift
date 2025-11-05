package com.github.kjetilv.uplift.util;

import module java.base;

public final class Throwables {

    public static String summary(Throwable throwable) {
        return chain(throwable)
            .map(Throwable::toString)
            .collect(
                Collectors.joining(" -> "));
    }

    public static Stream<Throwable> chain(Throwable throwable) {
        return Stream.iterate(throwable, Objects::nonNull, Throwables::next);
    }

    private Throwables() {
    }

    private static Throwable next(Throwable walker) {
        var cause = walker.getCause();
        return cause == walker ? null : cause;
    }
}
