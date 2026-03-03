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

    public static boolean clientFailure(Exception e) {
        return chain(e)
            .filter(IOException.class::isInstance)
            .map(Throwable::getMessage)
            .anyMatch(clientFailure());
    }

    private Throwables() {
    }

    private static final Set<String> IO_EXCEPTION_MESSAGES = Set.of(
        "Broken pipe",
        "Connection reset by peer",
        "Connection reset"
    );

    private static Throwable next(Throwable walker) {
        var cause = walker.getCause();
        return cause == walker ? null : cause;
    }

    private static Predicate<String> clientFailure() {
        return msg -> {
            var firstChar = msg.charAt(0);
            return (firstChar == 'B' || firstChar == 'C') &&
                   IO_EXCEPTION_MESSAGES.contains(msg);
        };
    }
}
