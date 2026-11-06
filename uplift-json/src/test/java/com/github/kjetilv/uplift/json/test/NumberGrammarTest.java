package com.github.kjetilv.uplift.json.test;

import module java.base;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.io.ReadException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/// Numbers must follow the JSON grammar: `-? (0 | [1-9][0-9]*) (.[0-9]+)? ([eE][+-]?[0-9]+)?`
class NumberGrammarTest {

    @Test
    void acceptsValidNumbers() {
        List.of("0", "-0", "7", "123", "-123", "1.5", "-0.25", "1e5", "1E+5", "1.5e-3", "0.0", "10E2")
            .forEach(number -> {
                var read = JSON.read(number);
                var expected = new BigDecimal(number);
                assertThat(read)
                    .as(number)
                    .isEqualTo(expected.scale() == 0 ? (Object) expected.longValue() : expected);
            });
    }

    @Test
    void rejectsInvalidNumbers() {
        List.of(".5", "007", "01", "1.", "-", "--1", "-.5", "1.2.3", "1e", "1e+", "1.e5", "+1", "-01")
            .forEach(number ->
                assertThatThrownBy(() -> JSON.read(number))
                    .as(number)
                    .isInstanceOf(ReadException.class));
    }

    @Test
    void rejectsInvalidNumbersInStructures() {
        assertThatThrownBy(() -> JSON.read("[007]")).isInstanceOf(ReadException.class);
        assertThatThrownBy(() -> JSON.read("{\"a\": 1.}")).isInstanceOf(ReadException.class);
        assertThatThrownBy(() -> JSON.read("{\"a\": .5}")).isInstanceOf(ReadException.class);
    }

    @Test
    void readsLongRangeIntegersAsLong() {
        assertThat(JSON.read(String.valueOf(Long.MAX_VALUE))).isEqualTo(Long.MAX_VALUE);
        assertThat(JSON.read(String.valueOf(Long.MIN_VALUE))).isEqualTo(Long.MIN_VALUE);
        assertThat(JSON.read("999999999999999999")).isEqualTo(999999999999999999L);
    }

    @Test
    void readsIntegersBeyondLongAsBigInteger() {
        var beyondMax = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        var beyondMin = BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE);
        var huge = new BigInteger("123456789012345678901234567890");

        assertThat(JSON.read(beyondMax.toString())).isEqualTo(beyondMax);
        assertThat(JSON.read(beyondMin.toString())).isEqualTo(beyondMin);
        assertThat(JSON.read("[" + huge + "]")).isEqualTo(List.of(huge));
        assertThat(JSON.read("1" + "0".repeat(2000))).isEqualTo(new BigInteger("1" + "0".repeat(2000)));
    }

    @Test
    void readsLongNumbers() {
        var number = "0." + "1".repeat(2000);
        assertThat(JSON.read(number)).isEqualTo(new BigDecimal(number));
        assertThat(JSON.read("[" + number + "]")).isEqualTo(List.of(new BigDecimal(number)));
    }

    private static final Json JSON = Json.instance();
}
