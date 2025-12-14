package com.github.kjetilv.uplift.fq.paths;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DimensionsTest {

    @Test
    void test10and100() {
        var d = new Dimensions(1, 2, 9);

        ed(d, 0, 0);
        ed(d, 9, 0);
        ed(d, 42, 40);
        ed(d, 99, 90);
        ed(d, 100, 100);
        ed(d, 111, 100);
        ed(d, 199, 100);
        ed(d, 200, 200);
        ed(d, 201, 200);
        ed(d, 999, 900);
        ed(d, 1000, 1000);
        ed(d, 101001, 101000);
    }

    @Test
    void test100andMyriad() {
        var d = new Dimensions(2, 4, 9);

        ed(d, 0, 0);
        ed(d, 9, 0);
        ed(d, 42, 0);
        ed(d, 99, 0);
        ed(d, 100, 100);
        ed(d, 111, 100);
        ed(d, 199, 100);
        ed(d, 200, 200);
        ed(d, 201, 200);
        ed(d, 999, 900);
        ed(d, 1000, 1000);
        ed(d, 1999, 1000);
        ed(d, 9999, 9000);
        ed(d, 10_000, 10_000);
        ed(d, 99_999, 90_000);
        ed(d, 100_000, 100_000);
        ed(d, 111_111, 110_000);
    }

    private static void ed(Dimensions d, int no, int expected) {
        var ledge = d.ledge(no);
        assertThat(ledge.segment())
            .hasSize(9)
            .startsWith("000")
            .endsWith(String.valueOf(expected));
    }

}