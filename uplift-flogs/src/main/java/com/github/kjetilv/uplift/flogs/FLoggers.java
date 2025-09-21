package com.github.kjetilv.uplift.flogs;

import module java.base;

final class FLoggers {

    private final LogLevel logLevel;

    private final Consumer<String> printer;

    private final Supplier<Instant> time;

    private final Consumer<String> emergencyPrinter;

    private final LogFormatter<LogEntry> formatter;

    FLoggers(
        LogLevel logLevel,
        Consumer<String> printer,
        Supplier<Instant> time,
        LogFormatter<LogEntry> formatter
    ) {
        this.logLevel = logLevel == null ? LogLevel.INFO : logLevel;
        this.printer = printer == null ? System.out::println : printer;
        this.emergencyPrinter = System.err::println;
        this.time = Objects.requireNonNull(time, "time");
        this.formatter = formatter == null ? LogEntryFormatter.INSTANCE : formatter;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + logLevel + "]";
    }

    FLogger create(String name) {
        return new FLogger(name, logLevel, formatter, printer, emergencyPrinter, time);
    }
}
