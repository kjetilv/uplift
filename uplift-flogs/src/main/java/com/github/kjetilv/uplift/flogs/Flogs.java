package com.github.kjetilv.uplift.flogs;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@SuppressWarnings({ "UnusedReturnValue" })
public final class Flogs {

    public static void initialize(Function<Long, Optional<String>> threadNames) {
        Jul.setup(threadNames);
    }

    public static Logger get(Class<?> source) {
        return get(requireNonNull(source, "source").getName());
    }

    public static Logger get(String name) {
        return LOGGERS.computeIfAbsent(name, Jul::createLogger);
    }

    static {
        emergencySetup();
    }

    private Flogs() {
    }

    @SuppressWarnings("StaticCollection")
    private static final Map<String, Logger> LOGGERS = new ConcurrentHashMap<>();

    private static void emergencySetup() {
        Jul.setup(id -> Optional.ofNullable(String.valueOf(id)));
    }
}
