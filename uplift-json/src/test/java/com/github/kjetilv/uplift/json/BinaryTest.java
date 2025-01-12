package com.github.kjetilv.uplift.json;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BinaryTest {

    @Test
    void search2() {
        assertAllFound(new int[] {1, 3});
    }

    @Test
    void search3() {
        assertAllFound(new int[] {1, 2, 3});
    }

    @Test
    void search4() {
        assertAllFound(new int[] {1, 2, 3, 4561});
    }

    @Test
    void search9() {
        assertAllFound(new int[] {1, 3, 5, 6, 10, 17, 42, 1234, 5345435});
    }

    @Test
    void search10() {
        assertAllFound(new int[] {1, 3, 5, 6, 10, 17, 42, 1234, 2345, 4567});
    }

    private static void assertAllFound(int[] is) {
        for (int i = 0; i < is.length; i++) {
            assertFound(is, is[i]);
        }
    }

    private static void assertFound(int[] is, int toFind) {
        int expectedIndex = -1;
        for (int i = 0; i < is.length; i++) {
            if (is[i] == toFind) {
                expectedIndex = i;
            }
        }
        assertThat(expectedIndex).isNotNegative();
        int search;
        try {
            search = Binary.search(is, toFind);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to lookup " + toFind, e);
        }
        assertThat(search).isEqualTo(expectedIndex);
    }

}