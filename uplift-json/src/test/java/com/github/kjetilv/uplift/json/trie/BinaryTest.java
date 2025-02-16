package com.github.kjetilv.uplift.json.trie;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BinaryTest {

    @Test
    void search1() {
        assertAllFound(new int[] {1});
    }

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
        assertAllFound(IS);
    }

    private static final int[] IS = new int[] {1, 3, 5, 6, 10, 17, 42, 1234, 2345, 4567};

    private static final int[] NIS =
        new int[] {2, 4, 7, 8, 9, 11, 12, 13, 14, 15, 16, 18, 19, 20, 21, 21, 234234, 13123};

    private static void assertAllFound(int[] is) {
        for (int j : is) {
            assertFound(is, j);
        }
        for (int j : NIS) {
            assertNotFound(j);
        }
    }

    private static void assertNotFound(int j) {
        assertThat(Binary.search(IS, j)).isNegative();
    }

    private static void assertFound(int[] is, int toFind) {
        int expectedIndex = -1;
        for (int i = 0; i < is.length; i++) {
            if (is[i] == toFind) {
                expectedIndex = i;
            }
        }
        assertThat(expectedIndex).isNotNegative();
        int search = Binary.search(is, toFind);
        assertThat(search).isEqualTo(expectedIndex);
    }

}