package com.github.kjetilv.uplift.flogs.test;

import module java.base;
import com.github.kjetilv.uplift.flogs.FjulFormatter;
import com.github.kjetilv.uplift.flogs.FlogsContext;
import com.github.kjetilv.uplift.flogs.LogEntry;
import com.github.kjetilv.uplift.flogs.LogFormatter;
import com.github.kjetilv.uplift.flogs.LogLevel;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/// The log context is bound for a scope, read through [MDC] or [FlogsContext], and written after the message.
class ContextTest {

    @Test
    void isEmptyOutsideScope() {
        assertThat(FlogsContext.get()).isEmpty();
        assertThat(MDC.get("a")).isNull();
    }

    @Test
    void isBoundForTheScopeOnly() {
        FlogsContext.where("a", "1").where("b", "2").run(() -> {
            assertThat(FlogsContext.get()).containsExactly(Map.entry("a", "1"), Map.entry("b", "2"));
            assertThat(MDC.get("a")).isEqualTo("1");
        });
        assertThat(FlogsContext.get()).isEmpty();
    }

    @Test
    void nestedScopesAddReplaceAndRemove() {
        FlogsContext.where("a", "1").where("b", "2").run(() -> {
            FlogsContext.where("b", "3").where("c", "4").where("a", null).run(() ->
                assertThat(FlogsContext.get()).containsExactly(Map.entry("b", "3"), Map.entry("c", "4")));
            assertThat(FlogsContext.get()).containsExactly(Map.entry("a", "1"), Map.entry("b", "2"));
        });
    }

    @Test
    void keepsInsertionOrder() {
        var context = new LinkedHashMap<String, String>();
        context.put("z", "1");
        context.put("a", "2");
        context.put("m", "3");
        FlogsContext.where(context).run(() ->
            assertThat(FlogsContext.get().keySet()).containsExactly("z", "a", "m"));
    }

    @Test
    void callReturnsValueAndPassesException() {
        assertThat(FlogsContext.where("a", "1").call(() -> MDC.get("a"))).isEqualTo("1");
        assertThatThrownBy(() -> FlogsContext.where("a", "1").call(() -> {
            throw new IOException("boom");
        })).isInstanceOf(IOException.class);
    }

    @Test
    void rejectsNullKey() {
        assertThatThrownBy(() -> FlogsContext.where(null, "x")).isInstanceOf(IllegalArgumentException.class);
        var context = new HashMap<String, String>();
        context.put(null, "x");
        assertThatThrownBy(() -> FlogsContext.where(context)).isInstanceOf(IllegalArgumentException.class);
        assertThat(FlogsContext.get(null)).isNull();
    }

    @Test
    void scopeIsNotChangedByItsInput() {
        var input = new LinkedHashMap<String, String>();
        input.put("a", "1");
        var scope = FlogsContext.where(input);
        input.put("b", "2");
        scope.run(() -> {
            assertThat(FlogsContext.get()).containsExactly(Map.entry("a", "1"));
            assertThatThrownBy(() -> FlogsContext.get().put("c", "3"))
                .isInstanceOf(UnsupportedOperationException.class);
        });
    }

    @Test
    void mdcChangesHaveNoEffect() {
        FlogsContext.where("a", "1").run(() -> {
            assertThatCode(() -> {
                MDC.put("b", "2");
                MDC.remove("a");
                MDC.setContextMap(Map.of("c", "3"));
                try (var _ = MDC.putCloseable("d", "4")) {
                    MDC.getMDCAdapter().put("e", "5");
                }
                MDC.clear();
            }).doesNotThrowAnyException();
            assertThat(FlogsContext.get()).containsExactly(Map.entry("a", "1"));
        });
    }

    @Test
    void mdcCopyIsModifiable() {
        FlogsContext.where("a", "1").run(() -> {
            var copy = MDC.getCopyOfContextMap();
            copy.put("b", "2");
            assertThat(copy).containsExactly(Map.entry("a", "1"), Map.entry("b", "2"));
            assertThat(MDC.get("b")).isNull();
        });
    }

    @Test
    void executorTasksDoNotGetContext() {
        FlogsContext.where("a", "1").run(() -> {
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                assertThat(executor.submit(() -> FlogsContext.get()).get()).isEmpty();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
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
    void julFormatterUsesContextInEffect() {
        var record = new LogRecord(Level.INFO, "jul");
        record.setLoggerName("jul.Logger");
        FlogsContext.where("req", "r1").run(() ->
            assertThat(new FjulFormatter.Default().format(record)).contains("jul {req=r1} 🧵"));
    }

    private static String format(Map<String, String> context) {
        return LogFormatter.BRIEF.format(new LogEntry(
            Instant.now(), "Name", "Name", LogLevel.INFO, "msg", null, new Object[0], false, 1L, "t", false, context
        ));
    }
}
