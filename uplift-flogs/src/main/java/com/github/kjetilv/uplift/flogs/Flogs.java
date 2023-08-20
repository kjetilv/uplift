package com.github.kjetilv.uplift.flogs;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

@SuppressWarnings({  "unused" })
public final class Flogs {

    public static void initialize() {
        initialize(null, null, null);
    }

    public static void initialize(LogLevel logLevel) {
        initialize(logLevel, null, null);
    }

    public static void initialize(ExecutorService background) {
        initialize(null, null, background);
    }

    public static void initialize(LogLevel logLevel, ExecutorService background) {
        initialize(logLevel, null, background);
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
        return Optional.ofNullable(simpleLoggers.get())
            .map(logger ->
                loggers.computeIfAbsent(name, logger::create))
            .orElseGet(() ->
                emergencyLoggers.create(name)
            );
    }

    public static void close() {
        stop();
    }

    private Flogs() {
    }

    @SuppressWarnings("StaticCollection")
    private static final Map<String, Logger> loggers = new ConcurrentHashMap<>();

    private static final AtomicReference<FLoggers> simpleLoggers = new AtomicReference<>();

    private static final FLoggers emergencyLoggers = initialized(null, null, null);

    private static FLoggers initialized(
        LogLevel logLevel,
        Consumer<String> printer,
        ExecutorService background
    ) {
        FLoggers loggers = simpleLoggers.updateAndGet(current ->
            new FLoggers(logLevel, printer, background));
        if (background != null) {
            Runtime.getRuntime().addShutdownHook(new Thread(Flogs::stop, "stop logger"));
        }
        return loggers;
    }

    private static void stop() {
        simpleLoggers.updateAndGet(loggers -> {
            if (loggers != null) {
                loggers.close();
            }
            return null;
        });
    }
}
