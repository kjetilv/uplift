package com.github.kjetilv.uplift.flogs;

import module java.base;

import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/// A [java.util.logging] handler that passes records to flogs, so that code using the JUL API logs through
/// flogs like code using [System.Logger], SLF4J and Commons Logging.
///
/// Install it with [#install()], or configure it in `logging.properties`:
/// `handlers = com.github.kjetilv.uplift.flogs.FlogsHandler`.
///
/// Records are passed on in the thread that logs them, so flogs records that thread. The message is formatted
/// by [Formatter#formatMessage(LogRecord)], so JUL's `{0}` parameters and resource bundles apply, and is then
/// passed to flogs without parameters. Levels are mapped to the nearest [LogLevel]. The time of a line is the
/// time flogs writes it, not the time in the record.
public final class FlogsHandler extends Handler {

    /// Makes flogs the only handler of the root JUL logger. Other root handlers, such as the default console
    /// handler, are removed. The root level is set to let through the records that flogs logs at its current
    /// level, so call this after [Flogs#initialize()]. Calling it again only updates the root level.
    public static void install() {
        var root = LogManager.getLogManager().getLogger("");
        Arrays.stream(root.getHandlers())
            .filter(handler -> !(handler instanceof FlogsHandler))
            .forEach(root::removeHandler);
        if (root.getHandlers().length == 0) {
            root.addHandler(new FlogsHandler());
        }
        root.setLevel(JulLevels.toJul(Flogs.logLevel()));
    }

    private final Function<String, Logger> floggers;

    /// Used by [#install()] and by `logging.properties`. Loggers come from [Flogs#get(String)].
    public FlogsHandler() {
        this(null);
    }

    /// @param floggers Creates the flogs logger for a name
    public FlogsHandler(Function<String, Logger> floggers) {
        this.floggers = floggers == null ? Flogs::get : floggers;
    }

    @Override
    public void publish(LogRecord record) {
        if (record == null || !isLoggable(record)) {
            return;
        }
        try {
            var level = JulLevels.toFlogs(record.getLevel());
            var flogger = floggers.apply(record.getLoggerName() == null ? "" : record.getLoggerName());
            if (flogger.isEnabled(level)) {
                var message = MESSAGES.formatMessage(record);
                var thrown = record.getThrown();
                if (thrown == null) {
                    flogger.log(level, message);
                } else {
                    flogger.log(level, message, thrown);
                }
            }
        } catch (Exception e) {
            reportError("Failed to pass record to flogs", e, ErrorManager.WRITE_FAILURE);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

    /// Formats messages only. [Formatter#formatMessage(LogRecord)] keeps no state.
    private static final Formatter MESSAGES = new Formatter() {

        @Override
        public String format(LogRecord record) {
            return formatMessage(record);
        }
    };

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getLevel() + "]";
    }
}
