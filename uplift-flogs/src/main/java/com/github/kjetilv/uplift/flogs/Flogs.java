package com.github.kjetilv.uplift.flogs;

import module java.base;

import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public final class Flogs {

    public static org.slf4j.Logger initializeAndGet(String logger) {
        initialize(null, null, null, null);
        return LoggerFactory.getLogger(logger);
    }

    public static org.slf4j.Logger initializeAndGet(String logger, LogLevel logLevel) {
        initialize(logLevel, null, null, null);
        return LoggerFactory.getLogger(logger);
    }

    public static org.slf4j.Logger initializeAndGet(String logger, LogLevel logLevel, Consumer<String> printer) {
        initialize(logLevel, printer, null, null);
        return LoggerFactory.getLogger(logger);
    }

    public static org.slf4j.Logger initializeAndGet(String logger, LogFormatter<LogEntry> formatter) {
        initialize(null, formatter);
        return LoggerFactory.getLogger(logger);
    }

    public static org.slf4j.Logger initializeAndGet(
        String logger,
        LogLevel logLevel,
        LogFormatter<LogEntry> formatter
    ) {
        initialize(logLevel, null, null, formatter);
        return LoggerFactory.getLogger(logger);
    }

    public static org.slf4j.Logger initializeAndGet(
        String logger,
        LogLevel logLevel,
        Consumer<String> printer,
        Supplier<Instant> time,
        LogFormatter<LogEntry> formatter
    ) {
        settings.orElseSet(() -> new Settings(logLevel, printer, time, formatter));
        return LoggerFactory.getLogger(logger);
    }

    public static org.slf4j.Logger initializeAndGet(Class<?> logger) {
        initialize(null, null, null, null);
        return LoggerFactory.getLogger(logger);
    }

    public static org.slf4j.Logger initializeAndGet(Class<?> logger, LogLevel logLevel) {
        initialize(logLevel, null, null, null);
        return LoggerFactory.getLogger(logger);
    }

    public static org.slf4j.Logger initializeAndGet(Class<?> logger, LogLevel logLevel, Consumer<String> printer) {
        initialize(logLevel, printer, null, null);
        return LoggerFactory.getLogger(logger);
    }

    public static org.slf4j.Logger initializeAndGet(Class<?> logger, LogFormatter<LogEntry> formatter) {
        initialize(null, formatter);
        return LoggerFactory.getLogger(logger);
    }

    public static org.slf4j.Logger initializeAndGet(
        Class<?> logger,
        LogLevel logLevel,
        LogFormatter<LogEntry> formatter
    ) {
        initialize(logLevel, null, null, formatter);
        return LoggerFactory.getLogger(logger);
    }

    public static org.slf4j.Logger initializeAndGet(
        Class<?> logger,
        LogLevel logLevel,
        Consumer<String> printer,
        Supplier<Instant> time,
        LogFormatter<LogEntry> formatter
    ) {
        settings.orElseSet(() -> new Settings(logLevel, printer, time, formatter));
        return LoggerFactory.getLogger(logger);
    }

    public static void initialize() {
        initialize(null, null, null, null);
    }

    public static void initialize(LogLevel logLevel) {
        initialize(logLevel, null, null, null);
    }

    public static void initialize(LogLevel logLevel, Consumer<String> printer) {
        initialize(logLevel, printer, null, null);
    }

    public static void initialize(LogFormatter<LogEntry> formatter) {
        initialize(null, formatter);
    }

    public static void initialize(LogLevel logLevel, LogFormatter<LogEntry> formatter) {
        initialize(logLevel, null, null, formatter);
    }

    public static void initialize(
        LogLevel logLevel,
        Consumer<String> printer,
        Supplier<Instant> time,
        LogFormatter<LogEntry> formatter
    ) {
        settings.orElseSet(() -> new Settings(logLevel, printer, time, formatter));
    }

    public static Logger get(Class<?> source) {
        return get(requireNonNull(source, "source").getName());
    }

    public static Logger get(String name) {
        return loggers.computeIfAbsent(name, Flogs::flogger);
    }

    private Flogs() {
    }

    private static final Map<String, Logger> loggers = new ConcurrentHashMap<>();

    private static final StableValue<Settings> settings = StableValue.of();

    private static final Supplier<Floggers> floggers = StableValue.supplier(() -> {
        var settings = Flogs.settings.orElseSet(Settings::new);
        return new Floggers(settings.printer(), settings);
    });

    static {
        FjulFormatter.init();
    }

    private static Flogger flogger(String name) {
        return floggers.get().create(name);
    }

    record Settings(
        LogLevel logLevel,
        Consumer<String> printer,
        Supplier<Instant> time,
        LogFormatter<LogEntry> formatter
    ) {

        Settings(
            LogLevel logLevel,
            Consumer<String> printer,
            Supplier<Instant> time,
            LogFormatter<LogEntry> formatter
        ) {
            this.logLevel = logLevel == null ? LogLevel.DEFAULT : logLevel;
            this.printer = printer == null ? IO::println : printer;
            this.time = time == null ? Instant::now : time;
            this.formatter = formatter == null ? LogFormatter.DEFAULT : formatter;
        }

        private Settings() {
            this(null, null, null, null);
        }

        public boolean isEnabled(LogLevel logLevel) {
            return this.logLevel.ordinal() >= logLevel.ordinal();
        }
    }
}
