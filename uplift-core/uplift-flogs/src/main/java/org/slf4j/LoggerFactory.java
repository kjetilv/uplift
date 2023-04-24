package org.slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.github.kjetilv.uplift.flogs.Flogs;

@SuppressWarnings({ "unused", "WeakerAccess" })
public final class LoggerFactory {

    public static ILoggerFactory getILoggerFactory() {
        return org.slf4j.LoggerFactory::getLogger;
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(Objects.requireNonNull(clazz, "clazz").getName());
    }

    public static Logger getLogger(String name) {
        return LOGGERS.computeIfAbsent(Objects.requireNonNull(name, "name"), __ ->
            new FLogger(name, Flogs.get(name)));
    }

    @SuppressWarnings("StaticCollection")
    private static final Map<String, Logger> LOGGERS = new ConcurrentHashMap<>();
}
