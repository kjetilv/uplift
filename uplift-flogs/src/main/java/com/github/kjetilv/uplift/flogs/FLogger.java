package com.github.kjetilv.uplift.flogs;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public final class FLogger implements Logger {

    public static final Object[] NO_ARGS = new Object[0];

    private final String name;

    private final LogLevel logLevel;

    private final Function<LogEntry, String> formatter;

    private final Consumer<String> linesWriter;

    private final Consumer<String> emergencyWriter;

    private final String sourceName;

    FLogger(
        String name,
        LogLevel logLevel,
        Function<LogEntry, String> formatter,
        Consumer<String> linesWriter,
        Consumer<String> emergencyWriter,
        String... knownPrefixes
    ) {
        this.sourceName = requireNonNull(name, "name");
        this.logLevel = requireNonNull(logLevel, "logLevel");
        this.formatter = requireNonNull(formatter, "formatter");
        this.linesWriter = requireNonNull(linesWriter, "linesWriter");
        this.emergencyWriter = emergencyWriter == null ? System.out::println : emergencyWriter;
        int lastDot = this.sourceName.lastIndexOf('.');
        if (lastDot < 0) {
            this.name = sourceName;
        } else {
            this.name = shorten(sourceName);
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isEnabled(LogLevel logLevel) {
        return this.logLevel.ordinal() >= logLevel.ordinal();
    }

    @Override
    public void log(LogLevel level, String msg, Object... args) {
        if (this.isEnabled(logLevel)) {
            doLog(level, msg, args);
        }
    }

    private void log(LogLevel logLevel, String format, Object arg1, Object arg2, Object arg3) {
        doLog(logLevel, format, arg1, arg2, arg3);
    }

    private void doLog(LogLevel level, String msg, Object... args) {
        if (this.isEnabled(level)) {
            LogEntry logEntry = LogEntry.create(name, level, msg, args);
            String logLine;
            try {
                logLine = formatter.apply(logEntry);
            } catch (Exception ex) {
                STDERR.println("Logging failed: " + logEntry);
                ex.printStackTrace(STDERR);
                logLine = "FATAL " + logEntry + ": " + ex;
            }
            try {
                linesWriter.accept(logLine);
            } catch (Exception e) {
                emergencyWriter.accept(logLine);
            }
        }
    }

    private static final PrintStream STDERR = System.err;

    private static String shorten(String sourceName) {
        String[] split = sourceName.split("\\.");
        String className = split[split.length - 1];
        AtomicInteger count = new AtomicInteger(1);
        return Arrays.stream(split)
                   .limit(split.length - 1)
                   .map(p -> {
                       int len = p.length();
                       return len < count.get()
                           ? p
                           : p.substring(0, Math.min(len, count.getAndIncrement()));
                   })
                   .collect(Collectors.joining(".")) + "." + className;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceName);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this ||
               obj instanceof FLogger logger &&
               Objects.equals(sourceName, logger.sourceName);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + "]";
    }
}
