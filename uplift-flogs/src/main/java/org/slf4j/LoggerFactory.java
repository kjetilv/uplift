package org.slf4j;

import com.github.kjetilv.uplift.flogs.Flogs;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class LoggerFactory {

    public static ILoggerFactory getILoggerFactory() {
        return org.slf4j.LoggerFactory::getLogger;
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(Objects.requireNonNull(clazz, "clazz").getName());
    }

    public static Logger getLogger(String name) {
        return LOGGERS.computeIfAbsent(
            Objects.requireNonNull(name, "name"),
            newLogger()
        );
    }

    private LoggerFactory() {
    }

    @SuppressWarnings("StaticCollection")
    private static final Map<String, Logger> LOGGERS = new HashMap<>();

    private static Function<String, Logger> newLogger() {
        return name ->
            new Slf4jLogger(name, Flogs.get(name));
    }
}
