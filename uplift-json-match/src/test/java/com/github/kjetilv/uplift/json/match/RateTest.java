package com.github.kjetilv.uplift.json.match;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateTest {

    @Test
    public void test() {
        assertEquals(
            Rate.of(7, 72),
            Rate.of(2, 48).plus(Rate.of(1, 18))
        );
    }
}
