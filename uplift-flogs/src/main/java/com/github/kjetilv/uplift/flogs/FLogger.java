package com.github.kjetilv.uplift.flogs;

import module java.base;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public final class FLogger implements Logger {

    public static final Object[] NO_ARGS = new Object[0];

    private final String name;

    private final String shortName;

    private final Flogs.Settings settings;

    private final String sourceName;

    private final Consumer<String> writer;

    private final Consumer<String> emergencyWriter;

    FLogger(String name, Consumer<String> writer, Consumer<String> emergencyWriter, Flogs.Settings settings) {
        this.sourceName = requireNonNull(name, "name");
        this.writer = requireNonNull(writer, "linesWriter");
        this.emergencyWriter = emergencyWriter == null ? System.err::println : emergencyWriter;
        this.settings = settings;
        var lastDot = this.sourceName.lastIndexOf('.');
        if (lastDot < 0) {
            this.name = sourceName;
            this.shortName = sourceName;
        } else {
            this.name = shorten(sourceName);
            this.shortName = sourceName.substring(lastDot + 1);
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String name(boolean shorten) {
        return shorten ? shortName : name;
    }

    @Override
    public boolean isEnabled(LogLevel logLevel) {
        return settings.isEnabled(logLevel);
    }

    @Override
    public void log(LogLevel level, String msg, Object... args) {
        if (settings.isEnabled(level)) {
            var logEntry = entry(level, msg, args);
            var logLine = logLine(logEntry);
            write(logLine);
        }
    }

    private LogEntry entry(LogLevel level, String msg, Object[] args) {
        return LogEntry.create(settings.time().get(), name, shortName, level, msg, args);
    }

    private String logLine(LogEntry logEntry) {
        try {
            return settings.formatter().format(logEntry);
        } catch (Exception e) {
            STDERR.println("Logging failed: " + logEntry);
            e.printStackTrace(STDERR);
            return "FATAL " + logEntry + ": " + e;
        }
    }

    private void write(String logLine) {
        try {
            writer.accept(logLine);
        } catch (Exception e) {
            try {
                emergencyWriter.accept(logLine);
            } finally {
                e.printStackTrace(STDERR);
                System.err.println("Log line could not be written: " + logLine);
            }
        }
    }

    private static final PrintStream STDERR = System.err;

    private static String shorten(String sourceName) {
        var split = sourceName.split("\\.");
        var className = split[split.length - 1];
        var count = new AtomicInteger(1);
        return Arrays.stream(split)
                   .limit(split.length - 1)
                   .map(p -> {
                       var len = p.length();
                       return len < count.get()
                           ? p
                           : p.substring(0, Math.min(len, count.getAndIncrement()));
                   })
                   .collect(Collectors.joining(".")) + "." + className;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this ||
               obj instanceof FLogger logger &&
               Objects.equals(sourceName, logger.sourceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceName);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + "]";
    }
}
