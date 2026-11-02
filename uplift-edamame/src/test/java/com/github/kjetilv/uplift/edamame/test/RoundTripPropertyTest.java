package com.github.kjetilv.uplift.edamame.test;

import module java.base;
import com.github.kjetilv.uplift.edamame.MapsMemoizer;
import com.github.kjetilv.uplift.edamame.MapsMemoizers;
import com.github.kjetilv.uplift.hash.HashKind;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/// Stores random trees, for both hash kinds, and checks:
/// * What is read back equals what was stored, with empty values left out
/// * Equal parts of all stored trees are the same instance
/// * An equal tree, built in a different key order, is the same instance
class RoundTripPropertyTest {

    @TestFactory
    Stream<DynamicTest> roundTrip() {
        return Stream.concat(roundTrips(HashKind.K128), roundTrips(HashKind.K256));
    }

    private static final List<Long> SEEDS = List.of(1L, 2L, 3L, 42L);

    private static final int TREES = 150;

    private static final int DEPTH = 4;

    private static <H extends HashKind<H>> Stream<DynamicTest> roundTrips(H kind) {
        return SEEDS.stream()
            .map(seed -> dynamicTest(kind + " seed " + seed, () -> roundTrip(kind, seed)));
    }

    private static <H extends HashKind<H>> void roundTrip(H kind, long seed) {
        var random = new Random(seed);
        MapsMemoizer<Integer, String> memoizer = MapsMemoizers.create(kind);
        var inputs = IntStream.range(0, TREES)
            .mapToObj(_ -> RandomTrees.map(random, DEPTH))
            .toList();

        IntStream.range(0, TREES).forEach(id -> memoizer.put(id, inputs.get(id)));
        IntStream.range(0, TREES).forEach(id ->
            memoizer.put(TREES + id, (Map<?, ?>) RandomTrees.shuffledCopy(inputs.get(id), random)));

        assertThat(memoizer.size()).isEqualTo(2 * TREES);
        IntStream.range(0, TREES).forEach(id -> {
            assertThat(memoizer.get(id))
                .as("tree %d", id)
                .isEqualTo(RandomTrees.expected(inputs.get(id)));
            assertThat(memoizer.get(TREES + id))
                .as("shuffled copy of tree %d", id)
                .isSameAs(memoizer.get(id));
        });

        var maps = memoizer.maps(true);
        RandomTrees.assertEqualPartsShared(
            IntStream.range(0, 2 * TREES)
                .mapToObj(maps::get)
                .toList()
        );
    }
}
