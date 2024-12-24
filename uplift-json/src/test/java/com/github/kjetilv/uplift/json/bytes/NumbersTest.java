package com.github.kjetilv.uplift.json.bytes;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class NumbersTest {

    @Test
    void negs() {
        assertThat(Numbers.parseNumber("foo: -1".getBytes(), 5, 2))
            .isEqualTo(-1L);
    }

    @Test
    void posses() {
        assertThat(Numbers.parseNumber("foo: 1234".getBytes(), 5, 4))
            .isEqualTo(1234L);
    }

    @Test
    void negDecs() {
        assertThat(Numbers.parseNumber("foo: -.1".getBytes(), 5, 3))
            .isEqualTo(new BigDecimal("-0.1"));
    }

    @Test
    void decs() {
        assertThat(Numbers.parseNumber("foo: .1".getBytes(), 5, 2))
            .isEqualTo(new BigDecimal(".1"));
        assertThat(Numbers.parseNumber("foo: 321.123".getBytes(), 5, 7))
            .isEqualTo(new BigDecimal("321.123"));
    }
}