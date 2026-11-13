package com.github.kjetilv.uplift.flogs;

import module java.base;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/// A [java.util.logging] formatter that writes records in the flogs format. Configure it for a handler, for
/// example `java.util.logging.ConsoleHandler.formatter = com.github.kjetilv.uplift.flogs.FjulFormatter$Default`.
///
/// The message is formatted by [Formatter#formatMessage(LogRecord)], so JUL's own `{0}` parameters and
/// resource bundles apply. Levels are mapped to the nearest [LogLevel].
///
/// The [FlogsContext] is read when the record is formatted. The standard JUL handlers format on the thread
/// that logs, so the line gets the context of that thread. A handler that formats on an other thread gives
/// the line the context of that other thread.
@SuppressWarnings("unused")
public abstract sealed class FjulFormatter extends Formatter {

    private final LogFormatter<LogEntry> formatter;

    protected FjulFormatter(LogFormatter<LogEntry> formatter) {
        this.formatter = Objects.requireNonNull(formatter, "formatter");
    }

    @Override
    public String format(LogRecord record) {
        var name = record.getLoggerName() == null ? "" : record.getLoggerName();
        return formatter.format(
            new LogEntry(
                record.getInstant(),
                name,
                name.substring(name.lastIndexOf('.') + 1),
                JulLevels.toFlogs(record.getLevel()),
                String.valueOf(formatMessage(record)),
                record.getThrown(),
                NO_ARGS,
                false,
                record.getLongThreadID(),
                String.valueOf(record.getLongThreadID()),
                null,
                FlogsContext.get()
            )
        );
    }

    private static final Object[] NO_ARGS = {};

    public static final class Default extends FjulFormatter {
        public Default() {
            super(LogFormatter.DEFAULT);
        }
    }

    public static final class Brief extends FjulFormatter {
        public Brief() {
            super(LogFormatter.BRIEF);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + formatter + "]";
    }
}
