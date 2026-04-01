package com.github.kjetilv.uplift.flogs;

import java.net.URL;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

@SuppressWarnings("unused")
public abstract sealed class JulFormatter extends Formatter {

    static void init() {
        if (inited.compareAndSet(false, true) && System.getProperty(CONFIG_FILE) == null) {
            Optional.ofNullable(Thread.currentThread().getContextClassLoader())
                .map(classLoader -> classLoader.getResource(LOGGING_PROPERTIES))
                .map(URL::getFile)
                .ifPresent(resource ->
                    System.setProperty(CONFIG_FILE, resource));
        }
    }

    private final LogFormatter<LogEntry> formatter;

    private JulFormatter(LogFormatter<LogEntry> formatter) {
        this.formatter = formatter;
    }

    @Override
    public String format(LogRecord record) {
        return formatter.format(
            new LogEntry(
                record.getInstant(),
                record.getLoggerName(),
                record.getLoggerName(),
                LogLevel.valueOf(record.getLevel().getName()),
                record.getMessage(),
                record.getThrown(),
                record.getParameters(),
                record.getThrown() != null,
                record.getLongThreadID(),
                String.valueOf(record.getLongThreadID())
            )
        );
    }

    private static final String CONFIG_FILE = "java.util.logging.config.file";

    private static final String LOGGING_PROPERTIES = "logging.properties";

    private static final AtomicBoolean inited = new AtomicBoolean();

    static {
        init();
    }

    public static final class Default extends JulFormatter {
        public Default() {
            super(LogFormatter.DEFAULT);
        }
    }

    public static final class Brief extends JulFormatter {
        public Brief() {
            super(LogFormatter.BRIEF);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + formatter + "]";
    }
}
