package com.github.kjetilv.uplift.flogs;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Objects.requireNonNull;

record LogEntry(
    Instant time,
    String name,
    LogLevel logLevel,
    String msg,
    Throwable throwable,
    Object[] args,
    boolean lastArgThrowable,
    long threadId,
    String threadName
) {

    @SuppressWarnings("deprecation")
    static LogEntry create(
        String name,
        LogLevel level,
        String msg,
        Object[] args
    ) {
        Thread thread = Thread.currentThread();
        Throwable argumentThrowable = argumentThrowable(args);
        LogEntry logEntry = new LogEntry(
            Instant.now(),
            name,
            level,
            msg,
            argumentThrowable,
            args,
            argumentThrowable != null,
            thread.getId(),
            thread.getName()
        );
        return logEntry;
    }

    public LogEntry {
        requireNonNull(time, "time");
        requireNonNull(name, "name");
        requireNonNull(logLevel, "logLevel");
        requireNonNull(msg, "msg");
        requireNonNull(args, "args");
        if (lastArgThrowable && args.length == 0) {
            throw new IllegalArgumentException(
                "No args, yet last arg should be throwable, msg was: `" + msg + "`", throwable);
        }
    }

    ZonedDateTime zuluTime() {
        return time.truncatedTo(MILLIS).atZone(Z);
    }

    boolean isInfo() {
        return logLevel == LogLevel.INFO;
    }

    private static final ZoneId Z = ZoneId.of("Z");

    private static Throwable argumentThrowable(Object[] args) {
        return args.length > 0 && args[args.length - 1] instanceof Throwable lastArgThrowable ? lastArgThrowable
            : null;
    }
}
