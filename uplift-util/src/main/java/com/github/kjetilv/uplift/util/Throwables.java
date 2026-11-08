package com.github.kjetilv.uplift.util;

import module java.base;

public final class Throwables {

    public static String summary(Throwable throwable) {
        return chain(throwable)
            .map(Throwable::toString)
            .collect(
                Collectors.joining(" -> "));
    }

    public static boolean clientFailure(Exception e) {
        return chain(e)
            .filter(IOException.class::isInstance)
            .map(Throwable::getMessage)
            .anyMatch(clientFailure());
    }

    public static Stream<Throwable> chain(Throwable t) {
        return chain(t, false);
    }

    @SuppressWarnings("SameParameterValue")
    public static Stream<Throwable> chain(Throwable throwable, boolean simple) {
        if (throwable == null) {
            return Stream.empty();
        }
        var chain = Stream.iterate(throwable, Throwables::hasNext, Throwable::getCause);
        if (simple) {
            return chain;
        }
        return chain.flatMap(element ->
            Stream.concat(
                Stream.of(element),
                suppressed(element).flatMap(Throwables::chain)
            ));
    }

    private Throwables() {
    }

    private static final Set<String> IO_EXCEPTION_MESSAGES = Set.of(
        "Broken pipe",
        "Connection reset by peer",
        "Connection reset"
    );

    private static boolean hasNext(Throwable throwable) {
        return throwable != null && throwable.getCause() != throwable;
    }

    private static Predicate<String> clientFailure() {
        return msg -> {
            var firstChar = msg.charAt(0);
            return (firstChar == 'B' || firstChar == 'C') &&
                   IO_EXCEPTION_MESSAGES.contains(msg);
        };
    }

    private static Stream<Throwable> suppressed(Throwable throwable) {
        return Stream.ofNullable(throwable.getSuppressed()).flatMap(Arrays::stream);
    }
}
