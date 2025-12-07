package com.github.kjetilv.uplift.flogs;

import module java.base;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public final class Flogs {

    public static void initialize() {
        initialize(null, null, null, null);
    }

    public static void initialize(LogLevel logLevel) {
        initialize(logLevel, null, null, null);
    }

    public static void initialize(LogLevel logLevel, Consumer<String> printer) {
        initialize(logLevel, printer, null, null);
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

    private static final Supplier<FLoggers> floggers = StableValue.supplier(() -> {
        var settings = Flogs.settings.orElseSet(Settings::new);
        return new FLoggers(settings.printer(), settings);
    });

    private static FLogger flogger(String name) {
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
            this.printer = printer == null ? System.out::println : printer;
            this.time = time == null ? Instant::now : time;
            this.formatter = formatter == null ? LogEntryFormatter.INSTANCE : formatter;
        }

        private Settings() {
            this(null, null, null, null);
        }

        public boolean isEnabled(LogLevel logLevel) {
            return this.logLevel.ordinal() >= logLevel.ordinal();
        }
    }
}
