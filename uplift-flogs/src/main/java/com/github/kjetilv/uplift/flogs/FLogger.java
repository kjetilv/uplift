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

    private final Consumer<String> linesWriter;

    private final Consumer<String> emergencyWriter;

    FLogger(
        String name,
        Consumer<String> linesWriter,
        Consumer<String> emergencyWriter,
        Flogs.Settings settings,
        String... knownPrefixes
    ) {
        this.sourceName = requireNonNull(name, "name");
        this.linesWriter = requireNonNull(linesWriter, "linesWriter");
        this.emergencyWriter = emergencyWriter == null ? System.out::println : emergencyWriter;
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
            write(logLine(LogEntry.create(
                settings.time().get(),
                name,
                shortName,
                level,
                msg,
                args
            )));
        }
    }

    private void write(String logLine) {
        try {
            linesWriter.accept(logLine);
        } catch (Exception e) {
            emergencyWriter.accept(logLine);
        }
    }

    private String logLine(LogEntry logEntry) {
        try {
            return settings.formatter().apply(logEntry);
        } catch (Exception ex) {
            STDERR.println("Logging failed: " + logEntry);
            ex.printStackTrace(STDERR);
            return "FATAL " + logEntry + ": " + ex;
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
