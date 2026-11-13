package com.github.kjetilv.uplift.flogs;

import module java.base;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Objects.requireNonNull;

public record LogEntry(
    Instant time,
    String name,
    String shortName,
    LogLevel logLevel,
    String msg,
    Throwable throwable,
    Object[] args,
    boolean lastArgThrowable,
    long threadId,
    String threadName,
    Boolean virtual,
    Map<String, String> context
) {

    /// Creates an entry for the current thread, with the [FlogsContext] of the thread. A null message is
    /// logged as `null`, and null arguments as no arguments.
    @SuppressWarnings("deprecation")
    static LogEntry create(
        Instant time,
        String name,
        String shortName,
        LogLevel level,
        String msg,
        Object[] args
    ) {
        var thread = Thread.currentThread();
        var arguments = args == null ? NO_ARGS : args;
        var argumentThrowable = argumentThrowable(arguments);
        return new LogEntry(
            time,
            name,
            shortName,
            level,
            String.valueOf(msg),
            argumentThrowable,
            arguments,
            argumentThrowable != null,
            thread.getId(),
            thread.getName(),
            thread.isVirtual(),
            FlogsContext.get()
        );
    }

    /// The context map is not copied. A null context is the same as an empty context.
    public LogEntry {
        requireNonNull(time, "time");
        requireNonNull(name, "name");
        requireNonNull(shortName, "shortName");
        requireNonNull(logLevel, "logLevel");
        requireNonNull(msg, "msg");
        requireNonNull(args, "args");
        if (lastArgThrowable && args.length == 0) {
            throw new IllegalArgumentException(
                "No args, yet last arg should be throwable, msg was: `" + msg + "`", throwable);
        }
        context = context == null ? Map.of() : context;
    }

    /// @return The thread name, with common pool workers and virtual threads marked, or an empty string if
    ///     not known
    @Override
    public String threadName() {
        if (threadName == null) {
            return "";
        }
        return threadName.startsWith(WORKER)
            ? POOL + threadName.substring(WORKER.length())
            : (virtual != null && virtual ? GHOST : "") + threadName;
    }

    ZonedDateTime zuluTime() {
        return time.truncatedTo(MILLIS).atZone(Z);
    }

    boolean hasLevel() {
        return logLevel != null;
    }

    private static final Object[] NO_ARGS = {};

    private static final String WORKER = "ForkJoinPool.commonPool-worker-";

    private static final ZoneId Z = ZoneId.of("Z");

    private static final String POOL = "🎱 ";

    private static final String GHOST = "👻 ";

    private static Throwable argumentThrowable(Object[] args) {
        return args.length > 0 && args[args.length - 1] instanceof Throwable lastArgThrowable ? lastArgThrowable
            : null;
    }
}
