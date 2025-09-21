package com.github.kjetilv.uplift.flogs;

import module java.base;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public final class Flogs {

    public static void initialize() {
        initialize(null, null);
    }

    public static void initialize(LogLevel logLevel) {
        initialize(logLevel, null);
    }

    public static void initialize(LogLevel logLevel, Consumer<String> printer) {
        initialized(logLevel, printer);
    }

    public static Logger get(Class<?> source) {
        return get(requireNonNull(source, "source").getName());
    }

    public static Logger get(String name) {
        return Optional.ofNullable(fLoggers.get())
            .map(logger ->
                loggers.computeIfAbsent(name, logger::create))
            .orElseGet(() ->
                emergencyFLoggers.create(name));
    }

    private Flogs() {
    }

    private static final Map<String, Logger> loggers = new ConcurrentHashMap<>();

    private static final AtomicReference<FLoggers> fLoggers = new AtomicReference<>();

    private static final FLoggers emergencyFLoggers = initialized(null, null);

    private static FLoggers initialized(
        LogLevel logLevel,
        Consumer<String> printer
    ) {
        return fLoggers.updateAndGet(current ->
            new FLoggers(logLevel, printer, Instant::now, null));
    }
}
