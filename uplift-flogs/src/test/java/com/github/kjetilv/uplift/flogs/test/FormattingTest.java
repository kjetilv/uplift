package com.github.kjetilv.uplift.flogs.test;

import module java.base;
import com.github.kjetilv.uplift.flogs.FjulFormatter;
import com.github.kjetilv.uplift.flogs.Flogs;
import com.github.kjetilv.uplift.flogs.LogEntry;
import com.github.kjetilv.uplift.flogs.LogFormatter;
import com.github.kjetilv.uplift.flogs.LogLevel;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/// Messages are formatted as SLF4J formats them, logging never throws to the caller, and JUL records are
/// formatted at their nearest level.
class FormattingTest {

    @Test
    void substitutesPlaceholders() {
        assertThat(message("Can't find {}", "x")).isEqualTo("Can't find x");
        assertThat(message("json {\"a\":1} {}", "x")).isEqualTo("json {\"a\":1} x");
        assertThat(message("a {} b {}", 1)).isEqualTo("a 1 b {}");
        assertThat(message("a {} b {}", 1, 2)).isEqualTo("a 1 b 2");
        assertThat(message("a {}", 1, 2)).isEqualTo("a 1");
        assertThat(message("no placeholder", 1)).isEqualTo("no placeholder");
        assertThat(message("{} and {}", null, "y")).isEqualTo("null and y");
        assertThat(message("it's {0} not {}", "slf4j")).isEqualTo("it's {0} not slf4j");
    }

    @Test
    void handlesEscapes() {
        assertThat(message("\\{} {}", "x")).isEqualTo("{} x");
        assertThat(message("\\\\{}", "x")).isEqualTo("\\x");
    }

    @Test
    void writesArraysAndFailingParameters() {
        var selfContaining = new Object[2];
        selfContaining[0] = "a";
        selfContaining[1] = selfContaining;
        assertThat(message("{}", (Object) new int[] {1, 2})).isEqualTo("[1, 2]");
        assertThat(message("{}", (Object) new Object[] {"a", new long[] {3}})).isEqualTo("[a, [3]]");
        assertThat(message("{}", (Object) selfContaining)).isEqualTo("[a, [...]]");
        assertThat(message("{}", new Object() {
            @Override
            public String toString() {
                throw new IllegalStateException("broken");
            }
        })).isEqualTo("[FAILED toString()]");
    }

    @Test
    void leavesOutTrailingThrowable() {
        var exception = new IllegalStateException("boom");
        var line = format(entry("failed {} {}", "x", exception));
        assertThat(line.lines().findFirst().orElseThrow()).contains("failed x {}");
        assertThat(line).contains("java.lang.IllegalStateException: boom");
    }

    @Test
    void doesNotModifyArguments() {
        var args = new Object[] {42, "s"};
        format(entry("{} {}", args));
        assertThat(args).containsExactly(42, "s");
        Flogs.get("no-modify").log(LogLevel.ERROR, "{} {}", args);
        assertThat(args).containsExactly(42, "s");
    }

    @Test
    void neverThrowsToTheCaller() {
        var logger = Flogs.get("null-safety");
        assertThatCode(() -> logger.log(LogLevel.ERROR, null)).doesNotThrowAnyException();
        assertThatCode(() -> logger.log(LogLevel.ERROR, "x {}", (Object[]) null)).doesNotThrowAnyException();
        assertThatCode(() -> LoggerFactory.getLogger("null-safety").error((String) null))
            .doesNotThrowAnyException();
    }

    @Test
    void acceptsUnknownThreadName() {
        var entry = new LogEntry(
            Instant.now(), "n", "n", LogLevel.INFO, "m", null, new Object[0], false, 1L, null, null, null
        );
        assertThat(entry.threadName()).isEmpty();
        assertThat(format(entry)).contains("m");
    }

    @Test
    void formatsJulRecordsAtTheirNearestLevel() {
        var formatter = new FjulFormatter.Default();
        Map.of(
            Level.SEVERE, "ERROR",
            Level.WARNING, "WARN",
            Level.INFO, "INFO",
            Level.CONFIG, "INFO",
            Level.FINE, "DEBUG",
            Level.FINEST, "TRACE"
        ).forEach((level, expected) -> {
            var record = new LogRecord(level, "jul {0} message");
            record.setLoggerName("jul.Logger");
            record.setParameters(new Object[] {"x"});
            assertThat(formatter.format(record))
                .describedAs("Level %s", level)
                .contains(expected + " jul.Logger jul x message");
        });
    }

    @Test
    void formatsJulRecordsWithoutMessageOrName() {
        var formatter = new FjulFormatter.Default();
        var record = new LogRecord(Level.SEVERE, null);
        record.setThrown(new IllegalStateException("boom"));
        var line = formatter.format(record);
        assertThat(line).contains("ERROR");
        assertThat(line).contains("java.lang.IllegalStateException: boom");
    }

    @Test
    void createsSlf4jLoggersConcurrently() throws Exception {
        try (var executor = Executors.newFixedThreadPool(8)) {
            var futures = IntStream.range(0, 2000)
                .mapToObj(i -> executor.submit(() -> LoggerFactory.getLogger("concurrent-" + i % 100)))
                .toList();
            for (var future : futures) {
                assertThat(future.get()).isNotNull();
            }
        }
        IntStream.range(0, 100).forEach(i ->
            assertThat(LoggerFactory.getLogger("concurrent-" + i)).isSameAs(LoggerFactory.getLogger("concurrent-" + i)));
    }

    private static String message(String message, Object... args) {
        var line = format(entry(message, args));
        var start = line.indexOf(" Name ") + " Name ".length();
        return line.substring(start, line.lastIndexOf(" 🧵"));
    }

    private static LogEntry entry(String message, Object... args) {
        var throwable = args.length > 0 && args[args.length - 1] instanceof Throwable t ? t : null;
        return new LogEntry(
            Instant.now(), "Name", "Name", LogLevel.INFO, message, throwable, args, throwable != null, 7L, "t", false, null
        );
    }

    private static String format(LogEntry entry) {
        return LogFormatter.BRIEF.format(entry);
    }
}
