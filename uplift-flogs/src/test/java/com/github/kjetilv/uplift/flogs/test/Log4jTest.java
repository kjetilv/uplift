package com.github.kjetilv.uplift.flogs.test;

import module java.base;
import com.github.kjetilv.uplift.flogs.FlogsContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.Supplier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/// The Log4j API sends each call to [Logger#log(Level, Marker, String, Object...)], calls suppliers only when
/// the level is enabled, and logs through flogs.
class Log4jTest {

    @Test
    void sendsLevelMethodsToTheirLevel() {
        var logger = new Recording(Level.TRACE);
        logger.trace("t");
        logger.debug("d");
        logger.info("i");
        logger.warn("w");
        logger.error("e");
        logger.fatal("f");
        logger.log(Level.INFO, "l");
        assertThat(logger.calls).extracting(Call::level)
            .containsExactly(Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL, Level.INFO);
    }

    @Test
    void passesParameters() {
        var logger = new Recording(Level.TRACE);
        logger.info("{}", 1);
        logger.info("{} {}", 1, 2);
        logger.info("{}", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        logger.info("{}", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        logger.info("{}", new Object[] {"a", "b"});
        assertThat(logger.calls).extracting(Call::params).containsExactly(
            List.of(1),
            List.of(1, 2),
            List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
            List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
            List.of("a", "b")
        );
    }

    @Test
    void passesThrowableAsLastParameter() {
        var logger = new Recording(Level.TRACE);
        var exception = new IllegalStateException("boom");
        logger.error("failed", exception);
        logger.error("failed {}", "x", exception);
        logger.error("failed", (Throwable) null);
        assertThat(logger.calls).extracting(Call::params).containsExactly(
            List.of(exception),
            List.of("x", exception),
            List.of()
        );
    }

    @Test
    void logsTextOfMessageObjects() {
        var logger = new Recording(Level.TRACE);
        var exception = new IllegalStateException("boom");
        logger.info((Object) 42);
        logger.info((CharSequence) new StringBuilder("chars {}"));
        logger.info(message("formatted {}", exception));
        logger.info(message("formatted", null), exception);
        assertThat(logger.calls).containsExactly(
            new Call(Level.INFO, null, "42", List.of()),
            new Call(Level.INFO, null, "chars {}", List.of()),
            new Call(Level.INFO, null, "formatted {}", List.of(exception)),
            new Call(Level.INFO, null, "formatted", List.of(exception))
        );
    }

    @Test
    void callsSuppliersOnlyWhenEnabled() {
        var logger = new Recording(Level.INFO);
        var called = new AtomicInteger();
        Supplier<String> supplier = () -> "supplied " + called.incrementAndGet();
        logger.debug(supplier);
        logger.debug("{}", supplier);
        logger.debug(() -> message("m", null));
        assertThat(called).hasValue(0);
        logger.info(supplier);
        logger.info("x {} {}", supplier, () -> message("m", null));
        logger.info(() -> message("from message supplier", null));
        assertThat(logger.calls).containsExactly(
            new Call(Level.INFO, null, "supplied 1", List.of()),
            new Call(Level.INFO, null, "x {} {}", List.of("supplied 2", "m")),
            new Call(Level.INFO, null, "from message supplier", List.of())
        );
    }

    @Test
    void passesMarkers() {
        var logger = new Recording(Level.TRACE);
        var marker = MarkerManager.getMarker("audit");
        logger.warn(marker, "w {}", 1);
        logger.log(Level.ERROR, marker, "e");
        assertThat(logger.calls).containsExactly(
            new Call(Level.WARN, marker, "w {}", List.of(1)),
            new Call(Level.ERROR, marker, "e", List.of())
        );
        assertThat(MarkerManager.getMarker("audit")).isSameAs(marker);
        assertThat(marker.isInstanceOf("audit")).isTrue();
    }

    @Test
    void formatsPrintfAndLogsCatchingAndThrowing() {
        var logger = new Recording(Level.TRACE);
        var exception = new IllegalStateException("boom");
        logger.printf(Level.INFO, "%s=%d", "a", 1);
        logger.printf(Level.INFO, "%d", "not a number");
        logger.catching(exception);
        assertThat(logger.throwing(Level.WARN, exception)).isSameAs(exception);
        assertThat(logger.calls).containsExactly(
            new Call(Level.INFO, null, "a=1", List.of()),
            new Call(Level.INFO, null, "%d [not a number]", List.of()),
            new Call(Level.ERROR, null, "Catching", List.of(exception)),
            new Call(Level.WARN, null, "Throwing", List.of(exception))
        );
    }

    @Test
    void fluentApiLogsNothing() {
        var logger = new Recording(Level.TRACE);
        var called = new AtomicBoolean();
        assertThat(logger.atInfo()).isSameAs(LogBuilder.NOOP);
        assertThat(logger.atLevel(Level.ERROR)).isSameAs(LogBuilder.NOOP);
        assertThatCode(() -> {
            logger.atError()
                .withMarker(MarkerManager.getMarker("m"))
                .withThrowable(new IllegalStateException("boom"))
                .withLocation()
                .log("x {}", 1);
            logger.atWarn().log("x {}", (Supplier<?>) () -> called.getAndSet(true));
            logger.always().log();
            assertThat(logger.atDebug().logAndGet(() -> message("m", null))).isNull();
        }).doesNotThrowAnyException();
        assertThat(called).isFalse();
        assertThat(logger.calls).isEmpty();
    }

    @Test
    void comparesAndFindsLevels() {
        assertThat(Level.ERROR.isMoreSpecificThan(Level.WARN)).isTrue();
        assertThat(Level.DEBUG.isLessSpecificThan(Level.INFO)).isTrue();
        assertThat(Level.INFO.isInRange(Level.ERROR, Level.DEBUG)).isTrue();
        assertThat(Level.toLevel("warn")).isSameAs(Level.WARN);
        assertThat(Level.toLevel("unknown")).isSameAs(Level.DEBUG);
        assertThat(Level.toLevel("unknown", Level.ERROR)).isSameAs(Level.ERROR);
        assertThat(Level.getLevel("unknown")).isNull();
        assertThatThrownBy(() -> Level.valueOf("unknown")).isInstanceOf(IllegalArgumentException.class);
        var notice = Level.forName("NOTICE", 350);
        assertThat(Level.forName("notice", 1)).isSameAs(notice);
        assertThat(Level.values())
            .isSorted()
            .containsSubsequence(
                Level.OFF, Level.FATAL, Level.ERROR, Level.WARN, notice, Level.INFO, Level.DEBUG, Level.TRACE, Level.ALL
            );
    }

    @Test
    void givesOneLoggerForEachName() {
        assertThat(LogManager.getLogger("a.b")).isSameAs(LogManager.getLogger("a.b"));
        assertThat(LogManager.getLogger(Log4jTest.class)).isSameAs(LogManager.getLogger());
        assertThat(LogManager.getLogger().getName()).isEqualTo(Log4jTest.class.getName());
        assertThat(LogManager.getLogger((String) null).getName()).isEqualTo(Log4jTest.class.getName());
        assertThat(LogManager.getRootLogger().getName()).isEmpty();
        assertThat(LogManager.getFormatterLogger("a.b")).isNotSameAs(LogManager.getLogger("a.b"));
    }

    @Test
    void mapsLevelsToFlogs() {
        var logger = LogManager.getLogger("log4j.levels");
        assertThat(logger.isEnabled(Level.OFF)).isFalse();
        assertThat(logger.isFatalEnabled()).isTrue();
        assertThat(logger.isErrorEnabled()).isTrue();
        assertThat(logger.isEnabled(Level.forName("SEVERE", 150))).isTrue();
        assertThat(logger.isEnabled(Level.ALL)).isEqualTo(logger.isTraceEnabled());
    }

    @Test
    void logsThroughFlogsWithoutThrowing() {
        var logger = LogManager.getLogger("log4j.flogs");
        var formatter = LogManager.getFormatterLogger("log4j.flogs");
        assertThatCode(() -> {
            logger.error("plain {}", 1, new IllegalStateException("boom"));
            logger.error((String) null);
            logger.log(Level.OFF, "never");
            formatter.error("%s and %d", "x", 2);
            formatter.error("%d", "not a number", new IllegalStateException("boom"));
        }).doesNotThrowAnyException();
    }

    @Test
    void threadContextReadsScopedContext() {
        FlogsContext.where("req", "r1").run(() -> {
            assertThat(ThreadContext.get("req")).isEqualTo("r1");
            assertThat(ThreadContext.containsKey("req")).isTrue();
            assertThat(ThreadContext.isEmpty()).isFalse();
            assertThat(ThreadContext.getImmutableContext()).containsExactly(Map.entry("req", "r1"));
            var copy = ThreadContext.getContext();
            copy.put("x", "y");
            assertThat(ThreadContext.get("x")).isNull();
        });
        assertThat(ThreadContext.isEmpty()).isTrue();
    }

    @Test
    void threadContextChangesHaveNoEffect() {
        FlogsContext.where("req", "r1").run(() -> {
            assertThatCode(() -> {
                ThreadContext.put("a", "1");
                ThreadContext.putAll(Map.of("b", "2"));
                ThreadContext.remove("req");
                ThreadContext.clearAll();
                ThreadContext.push("n");
                assertThat(ThreadContext.pop()).isEmpty();
            }).doesNotThrowAnyException();
            assertThat(ThreadContext.getImmutableContext()).containsExactly(Map.entry("req", "r1"));
            assertThat(ThreadContext.getDepth()).isZero();
        });
    }

    private static Message message(String text, Throwable throwable) {
        return new Message() {

            @Override
            public String getFormattedMessage() {
                return text;
            }

            @Override
            public Throwable getThrowable() {
                return throwable;
            }
        };
    }

    record Call(Level level, Marker marker, String message, List<Object> params) {
    }

    /// Records calls at and above a threshold
    private static final class Recording implements Logger {

        final List<Call> calls = new ArrayList<>();

        private final Level threshold;

        private Recording(Level threshold) {
            this.threshold = threshold;
        }

        @Override
        public String getName() {
            return "recording";
        }

        @Override
        public Level getLevel() {
            return threshold;
        }

        @Override
        public boolean isEnabled(Level level, Marker marker) {
            return level.isMoreSpecificThan(threshold);
        }

        @Override
        public void log(Level level, Marker marker, String message, Object... params) {
            if (isEnabled(level, marker)) {
                calls.add(new Call(level, marker, message, Arrays.asList(params)));
            }
        }
    }
}
