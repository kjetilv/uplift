package com.github.kjetilv.uplift.edam.patterns;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CyclesTest {

    @Test
    void findSimple() {
        var integers = Cycles.find(1, 2, 3, 1, 2, 3, 1, 2, 3);
        assertEquals(List.of(1, 2, 3), integers);
    }

    @Test
    void findSimpler() {
        var integers = Cycles.find(1, 1, 1, 1);
        assertEquals(List.of(1), integers);
    }

    @Test
    void findNone() {
        var integers = Cycles.find(1, 2, 3, 1, 2, 3, 1, 2, 3, 4);
        assertEquals(List.of(1, 2, 3, 1, 2, 3, 1, 2, 3, 4), integers);
    }
}