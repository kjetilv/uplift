package com.github.kjetilv.uplift.json.test.io;

import module java.base;
import com.github.kjetilv.uplift.json.FieldEvents;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.io.DefaultFieldEvents;
import com.github.kjetilv.uplift.json.io.Sink;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/// Numbers are formatted in one place, so untyped writes and field writes agree. Decimals use exponent
/// notation where [BigDecimal#toString()] does, and NaN and infinity are rejected.
class NumberWriteTest {

    @Test
    void decimalsUseToString() {
        Stream.of("0.0000001", "1.5e10", "1e100000", "1.5", "-0.25", "100")
            .map(BigDecimal::new)
            .forEach(decimal -> {
                assertThat(untyped(decimal)).isEqualTo(decimal.toString());
                assertThat(field(decimal)).isEqualTo(decimal.toString());
            });
    }

    @Test
    void decimalsReadFromJsonStayCompact() {
        var written = Json.instance().write(Json.instance().read("{\"a\":1e100000}"));
        assertThat(written).isEqualTo("{\"a\":1E+100000}");
    }

    @Test
    void otherNumbersAreUnchanged() {
        Stream.<Number>of(
            Long.MIN_VALUE,
            Integer.MAX_VALUE,
            (short) -7,
            (byte) 3,
            1.5e-7,
            0.1f,
            new BigInteger("123456789012345678901234567890"),
            new AtomicLong(42)
        ).forEach(number -> {
            assertThat(untyped(number)).isEqualTo(number.toString());
            assertThat(field(number)).isEqualTo(number.toString());
        });
    }

    @Test
    void rejectsNonFiniteNumbers() {
        Stream.<Number>of(
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Float.NaN,
            Float.POSITIVE_INFINITY
        ).forEach(number -> {
            assertThatThrownBy(() -> untyped(number))
                .describedAs("Untyped %s", number)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not a JSON number");
            assertThatThrownBy(() -> field(number))
                .describedAs("Field %s", number)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not a JSON number");
        });
        assertThatThrownBy(() -> write(events -> events.numberArray("d", new double[] {1.0, Double.NaN})))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> write(events -> events.numberArray("f", new float[] {Float.NEGATIVE_INFINITY})))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static String untyped(Number number) {
        var written = Json.instance().write(List.of(number));
        return written.substring(1, written.length() - 1);
    }

    private static String field(Number number) {
        var written = write(events -> events.number("n", number));
        return written.substring("{\"n\":".length(), written.length() - 1);
    }

    private static String write(Consumer<FieldEvents> writer) {
        var sb = new StringBuilder();
        var events = new DefaultFieldEvents(Sink.string(sb));
        writer.accept(events);
        events.done();
        return sb.toString();
    }
}
