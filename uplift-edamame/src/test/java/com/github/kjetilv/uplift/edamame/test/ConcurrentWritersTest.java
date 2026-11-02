package com.github.kjetilv.uplift.edamame.test;

import module java.base;
import com.github.kjetilv.uplift.edamame.MapsMemoizer;
import com.github.kjetilv.uplift.edamame.MapsMemoizers;
import com.github.kjetilv.uplift.hash.HashKind;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/// Several threads store the same identifiers at the same time, each with its own equal copies of the trees.
class ConcurrentWritersTest {

    @TestFactory
    Stream<DynamicTest> concurrentWriters() {
        return Stream.of(
            dynamicTest("K128", () -> concurrentWriters(HashKind.K128)),
            dynamicTest("K256", () -> concurrentWriters(HashKind.K256))
        );
    }

    private static final int THREADS = 8;

    private static final int IDS = 300;

    private static <H extends HashKind<H>> void concurrentWriters(H kind) throws Exception {
        MapsMemoizer<Integer, String> memoizer = MapsMemoizers.create(kind);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(THREADS)) {
            var stored = IntStream.range(0, THREADS)
                .mapToObj(thread -> executor.submit(() -> {
                    var ids = IntStream.range(0, IDS).boxed().collect(Collectors.toCollection(ArrayList::new));
                    Collections.shuffle(ids, new Random(thread));
                    start.await();
                    return ids.stream()
                        .filter(id -> memoizer.putIfAbsent(id, tree(id)))
                        .count();
                }))
                .toList();
            start.countDown();

            var total = 0L;
            for (var future : stored) {
                total += future.get(30, TimeUnit.SECONDS);
            }
            assertThat(total).as("each identifier stored by exactly one thread").isEqualTo(IDS);
        }

        assertThat(memoizer.size()).isEqualTo(IDS);
        IntStream.range(0, IDS).forEach(id ->
            assertThat(memoizer.get(id)).as("tree %d", id).isEqualTo(RandomTrees.expected(tree(id))));

        var maps = memoizer.maps(true);
        RandomTrees.assertEqualPartsShared(
            IntStream.range(0, IDS)
                .mapToObj(maps::get)
                .toList()
        );
    }

    /// @return A new instance of the same tree for each call with the same id
    private static Map<String, Object> tree(int id) {
        return RandomTrees.map(new Random(id % 100), 3);
    }
}
