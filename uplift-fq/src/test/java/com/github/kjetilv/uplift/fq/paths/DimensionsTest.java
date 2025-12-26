package com.github.kjetilv.uplift.fq.paths;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DimensionsTest {

    @Test
    void test10and100() {
        var dim = new Dimensions(1, 2, 9);

        expectLedge(dim, 0, 9, 0);
        expectLedge(dim, 9, 9, 0);
        expectLedge(dim, 42, 7, 40);
        expectLedge(dim, 99, 7, 90);
        expectLedge(dim, 100, 6, 100);
        expectLedge(dim, 111, 6, 100);
        expectLedge(dim, 199, 6, 100);
        expectLedge(dim, 200, 6, 200);
        expectLedge(dim, 201, 6, 200);
        expectLedge(dim, 999, 6, 900);
        expectLedge(dim, 1000, 5, 1000);
        expectLedge(dim, 101001, 3, 101000);
    }

    @Test
    void test100andMyriad() {
        var dim = new Dimensions(2, 4, 9);

        expectLedge(dim, 0, 9, 0);
        expectLedge(dim, 9, 9, 0);
        expectLedge(dim, 42, 9, 0);
        expectLedge(dim, 99, 9, 0);
        expectLedge(dim, 100, 6, 100);
        expectLedge(dim, 111, 6, 100);
        expectLedge(dim, 199, 6, 100);
        expectLedge(dim, 200, 6, 200);
        expectLedge(dim, 201, 6, 200);
        expectLedge(dim, 999, 6, 900);
        expectLedge(dim, 1000, 5, 1000);
        expectLedge(dim, 1999, 5, 1000);
        expectLedge(dim, 9999, 5, 9000);
        expectLedge(dim, 10_000, 4, 10_000);
        expectLedge(dim, 99_999, 4, 90_000);
        expectLedge(dim, 100_000, 3, 100_000);
        expectLedge(dim, 111_111, 3, 110_000);
        expectLedge(dim, 999_999, 3, 990_000);
        expectLedge(dim, 1_000_001, 2, 1_000_000);
    }

    private static void expectLedge(Dimensions d, int no, int zeroes, int expected) {
        var ledge = d.ledge(no);
        assertThat(ledge.asSegment())
            .hasSize(9)
            .startsWith("0000000000".substring(0, zeroes))
            .doesNotStartWith("0000000000".substring(0, zeroes + 1))
            .endsWith(String.valueOf(expected));
    }
}