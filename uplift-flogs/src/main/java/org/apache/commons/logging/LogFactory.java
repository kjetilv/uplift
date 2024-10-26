package org.apache.commons.logging;

import com.github.kjetilv.uplift.flogs.Flogs;
import com.github.kjetilv.uplift.flogs.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class LogFactory {

    public static Log getLog(Class<?> clazz) {
        return getLog(clazz.getName());
    }

    public static Log getLog(String name) {
        return logger(Flogs.get(name));
    }

    private LogFactory() {
    }

    private static final Map<String, Log> LOGGERS = new HashMap<>();

    private static Log logger(Logger logger) {
        return new CommonsLogger(logger);
    }

    private static Function<String, Log> newLogger() {
        return name -> new CommonsLogger(Flogs.get(name));
    }
}
