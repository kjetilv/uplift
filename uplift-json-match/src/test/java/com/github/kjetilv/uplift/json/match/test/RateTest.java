package com.github.kjetilv.uplift.json.match.test;

import com.github.kjetilv.uplift.json.match.Rate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RateTest {

    @Test
    public void test() {
        Assertions.assertEquals(
            Rate.of(7, 72),
            Rate.of(2, 48).plus(Rate.of(1, 18))
        );
    }
}
