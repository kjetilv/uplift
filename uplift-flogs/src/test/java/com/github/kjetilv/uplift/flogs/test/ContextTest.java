package com.github.kjetilv.uplift.flogs.test;

import module java.base;
import com.github.kjetilv.uplift.flogs.FjulFormatter;
import com.github.kjetilv.uplift.flogs.FlogsContext;
import com.github.kjetilv.uplift.flogs.LogEntry;
import com.github.kjetilv.uplift.flogs.LogFormatter;
import com.github.kjetilv.uplift.flogs.LogLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/// The log context is kept per thread, changed through [MDC] or [FlogsContext], and written after the message.
class ContextTest {

    @AfterEach
    void clear() {
        FlogsContext.clear();
    }

    @Test
    void putsGetsAndRemoves() {
        MDC.put("a", "1");
        MDC.put("b", "2");
        assertThat(MDC.get("a")).isEqualTo("1");
        assertThat(FlogsContext.get()).containsExactly(Map.entry("a", "1"), Map.entry("b", "2"));
        MDC.remove("a");
        assertThat(MDC.get("a")).isNull();
        assertThat(FlogsContext.get()).containsExactly(Map.entry("b", "2"));
        MDC.clear();
        assertThat(FlogsContext.get()).isEmpty();
    }

    @Test
    void keepsInsertionOrder() {
        MDC.put("z", "1");
        MDC.put("a", "2");
        MDC.put("m", "3");
        MDC.put("z", "4");
        assertThat(FlogsContext.get().keySet()).containsExactly("z", "a", "m");
        assertThat(MDC.get("z")).isEqualTo("4");
    }

    @Test
    void nullValueRemovesAndNullKeyIsRejected() {
        MDC.put("a", "1");
        MDC.put("a", null);
        assertThat(FlogsContext.get()).isEmpty();
        assertThatThrownBy(() -> MDC.put(null, "x")).isInstanceOf(IllegalArgumentException.class);
        assertThat(MDC.get(null)).isNull();
        MDC.remove(null);
    }

    @Test
    void contextIsUnmodifiableAndReplacedOnChange() {
        MDC.put("a", "1");
        var before = FlogsContext.get();
        MDC.put("b", "2");
        assertThat(before).containsExactly(Map.entry("a", "1"));
        assertThatThrownBy(() -> FlogsContext.get().put("c", "3"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void copiesAndSetsContextMap() {
        MDC.put("a", "1");
        var copy = MDC.getCopyOfContextMap();
        copy.put("b", "2");
        assertThat(MDC.get("b")).isNull();
        var input = new LinkedHashMap<String, String>();
        input.put("x", "1");
        input.put("y", null);
        MDC.setContextMap(input);
        input.put("z", "3");
        assertThat(FlogsContext.get()).containsExactly(Map.entry("x", "1"));
        MDC.setContextMap(null);
        assertThat(FlogsContext.get()).isEmpty();
    }

    @Test
    void closeableRemovesKey() {
        MDC.put("keep", "1");
        try (var _ = MDC.putCloseable("tmp", "2")) {
            assertThat(MDC.get("tmp")).isEqualTo("2");
        }
        assertThat(FlogsContext.get()).containsExactly(Map.entry("keep", "1"));
    }

    @Test
    void adapterUsesSameContext() {
        var adapter = MDC.getMDCAdapter();
        adapter.put("a", "1");
        assertThat(MDC.get("a")).isEqualTo("1");
        adapter.clear();
        assertThat(FlogsContext.get()).isEmpty();
    }

    @Test
    void contextStaysOnItsThread() throws Exception {
        MDC.put("a", "1");
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var other = executor.submit(() -> {
                var seen = FlogsContext.get();
                MDC.put("b", "2");
                return seen;
            }).get();
            assertThat(other).isEmpty();
        }
        assertThat(FlogsContext.get()).containsExactly(Map.entry("a", "1"));
    }

    @Test
    void writesContextAfterMessage() {
        var context = new LinkedHashMap<String, String>();
        context.put("req", "r1");
        context.put("user", "u1");
        assertThat(format(context)).contains("Name msg {req=r1, user=u1} 🧵");
    }

    @Test
    void writesNothingForEmptyContext() {
        assertThat(format(Map.of())).contains("Name msg 🧵");
        assertThat(format(null)).contains("Name msg 🧵");
    }

    @Test
    void julFormatterUsesContextOfThread() {
        MDC.put("req", "r1");
        var record = new LogRecord(Level.INFO, "jul");
        record.setLoggerName("jul.Logger");
        assertThat(new FjulFormatter.Default().format(record)).contains("jul {req=r1} 🧵");
    }

    private static String format(Map<String, String> context) {
        return LogFormatter.BRIEF.format(new LogEntry(
            Instant.now(), "Name", "Name", LogLevel.INFO, "msg", null, new Object[0], false, 1L, "t", false, context
        ));
    }
}
