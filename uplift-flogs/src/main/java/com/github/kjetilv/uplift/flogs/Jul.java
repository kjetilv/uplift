package com.github.kjetilv.uplift.flogs;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;

final class Jul {

    static void setup(Function<Long, Optional<String>> threadNames) {
        if (setupDone.compareAndSet(false, true)) {
            try {
                Logger root = LogManager.getLogManager().getLogger("");
                root.setLevel(INFO);
                root.setUseParentHandlers(false);
                for (Handler handler: root.getHandlers().clone()) {
                    root.removeHandler(handler);
                }
                root.addHandler(new StdoutConsoleHandler(
                    DefaultLogFormatter.get().orElseGet(() ->
                        new DefaultLogFormatter(threadNames))));
            } catch (Exception e) {
                throw new IllegalStateException("Setup failed", e);
            }
        }
    }

    static com.github.kjetilv.uplift.flogs.Logger createLogger(String name) {
        Logger logger = instrument(Logger.getLogger(name), name);
        return new com.github.kjetilv.uplift.flogs.Logger(logger);
    }

    private Jul() {
    }

    private static final AtomicBoolean setupDone = new AtomicBoolean();

    private static Logger instrument(Logger logger, String name) {
        boolean local = name.startsWith("mediaserver");
        logger.setLevel(FINE);
        logger.setFilter(record ->
            local || record.getLevel().intValue() >= INFO.intValue());
        logger.setUseParentHandlers(true);
        clearHandlers(logger);
        return logger;
    }

    private static void clearHandlers(Logger logger) {
        Handler[] handlers = logger.getHandlers();
        if (handlers != null && handlers.length > 0) {
            for (Handler h: handlers.clone()) {
                logger.removeHandler(h);
            }
        }
    }
}
