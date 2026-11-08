package com.github.kjetilv.uplift.flogs.test;

import module java.base;
import com.github.kjetilv.uplift.flogs.Flogs;
import com.github.kjetilv.uplift.flogs.FlogsHandler;
import com.github.kjetilv.uplift.flogs.LogLevel;
import org.junit.jupiter.api.Test;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import static org.assertj.core.api.Assertions.assertThat;

/// [FlogsHandler] passes JUL records to flogs, and [FlogsHandler#install()] makes it the only root handler.
class JulHandlerTest {

    @Test
    void passesRecordsLoggedThroughTheJulApi() {
        var capture = new Capture(LogLevel.TRACE);
        var logger = julLogger("jul.api", capture);
        logger.info("plain");
        logger.log(Level.WARNING, "with {0} and {1}", new Object[] {"x", 2});
        logger.log(Level.INFO, "it's {} here", "x");
        assertThat(capture.calls).containsExactly(
            new Capture.Call(LogLevel.INFO, "plain", List.of()),
            new Capture.Call(LogLevel.WARN, "with x and 2", List.of()),
            new Capture.Call(LogLevel.INFO, "it's {} here", List.of())
        );
    }

    @Test
    void mapsLevels() {
        var capture = new Capture(LogLevel.TRACE);
        var logger = julLogger("jul.levels", capture);
        logger.setLevel(Level.ALL);
        Stream.of(Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST)
            .forEach(level -> logger.log(level, "m"));
        assertThat(capture.calls).extracting(Capture.Call::level).containsExactly(
            LogLevel.ERROR, LogLevel.WARN, LogLevel.INFO, LogLevel.INFO, LogLevel.DEBUG, LogLevel.TRACE, LogLevel.TRACE
        );
    }

    @Test
    void passesThrowables() {
        var capture = new Capture(LogLevel.TRACE);
        var exception = new IllegalStateException("boom");
        julLogger("jul.thrown", capture).log(Level.SEVERE, "failed {}", exception);
        assertThat(capture.calls).containsExactly(new Capture.Call(LogLevel.ERROR, "failed {}", List.of(exception)));
    }

    @Test
    void skipsLevelsThatFlogsOrTheHandlerDoesNotLog() {
        var capture = new Capture(LogLevel.INFO);
        var handler = new FlogsHandler(_ -> capture);
        var record = new LogRecord(Level.FINE, "fine {0}");
        record.setParameters(new Object[] {new Object() {
            @Override
            public String toString() {
                throw new AssertionError("Formatted for a disabled level");
            }
        }});
        handler.publish(record);
        handler.setLevel(Level.WARNING);
        handler.publish(new LogRecord(Level.INFO, "below handler level"));
        handler.publish(null);
        assertThat(capture.calls).isEmpty();
    }

    @Test
    void installsAsTheOnlyRootHandler() {
        var root = LogManager.getLogManager().getLogger("");
        var handlers = root.getHandlers();
        var level = root.getLevel();
        try {
            var console = new ConsoleHandler();
            root.addHandler(console);
            FlogsHandler.install();
            FlogsHandler.install();
            assertThat(root.getHandlers()).hasSize(1).allMatch(FlogsHandler.class::isInstance);
            var expected = switch (Flogs.logLevel()) {
                case NONE -> Level.OFF;
                case ERROR -> Level.SEVERE;
                case WARN -> Level.WARNING;
                case INFO -> Level.CONFIG;
                case DEBUG -> Level.FINE;
                case TRACE -> Level.ALL;
            };
            assertThat(root.getLevel()).isEqualTo(expected);
        } finally {
            Arrays.stream(root.getHandlers()).forEach(root::removeHandler);
            Arrays.stream(handlers).forEach(root::addHandler);
            root.setLevel(level);
        }
    }

    /// @return A JUL logger that logs only to a handler for the capture
    private static java.util.logging.Logger julLogger(String name, Capture capture) {
        var logger = java.util.logging.Logger.getLogger(name);
        Arrays.stream(logger.getHandlers()).forEach(logger::removeHandler);
        logger.setUseParentHandlers(false);
        logger.addHandler(handler(capture));
        LOGGERS.add(logger);
        return logger;
    }

    private static Handler handler(Capture capture) {
        return new FlogsHandler(_ -> capture);
    }

    /// Holds the loggers, since JUL keeps only weak references to them
    private static final List<java.util.logging.Logger> LOGGERS = new CopyOnWriteArrayList<>();
}
