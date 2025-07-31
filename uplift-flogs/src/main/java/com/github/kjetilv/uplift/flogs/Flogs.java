package com.github.kjetilv.uplift.flogs;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public final class Flogs {

    public static void initialize() {
        initialize(null, null, null);
    }

    public static void initialize(LogLevel logLevel) {
        initialize(logLevel, null, null);
    }

    public static void initialize(LogLevel logLevel, Consumer<String> printer) {
        initialize(logLevel, printer, null);
    }

    public static void initialize(
        LogLevel logLevel,
        Consumer<String> printer,
        ExecutorService background
    ) {
        initialized(logLevel, printer, background);
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

    private static final FLoggers emergencyFLoggers = initialized(null, null, null);

    private static FLoggers initialized(
        LogLevel logLevel,
        Consumer<String> printer,
        ExecutorService background
    ) {
        return fLoggers.updateAndGet(current ->
            new FLoggers(logLevel, printer, Instant::now, null));
    }
}
