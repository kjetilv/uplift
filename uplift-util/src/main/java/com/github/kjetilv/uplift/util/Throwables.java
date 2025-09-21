package com.github.kjetilv.uplift.util;

import module java.base;

public final class Throwables {

    public static String summary(Throwable throwable) {
        return Stream.iterate(throwable, Objects::nonNull, Throwables::next)
            .map(Throwable::toString)
            .collect(
                Collectors.joining(" -> "));
    }

    private static Throwable next(Throwable walker) {
        Throwable cause = walker.getCause();
        return cause == walker ? null : cause;
    }

    private Throwables() {
    }
}
