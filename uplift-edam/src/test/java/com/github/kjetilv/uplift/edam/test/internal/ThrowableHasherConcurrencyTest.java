package com.github.kjetilv.uplift.edam.test.internal;

import module java.base;
import com.github.kjetilv.uplift.edam.internal.ThrowableHasher;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind.K128;
import org.junit.jupiter.api.Test;

import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static org.junit.jupiter.api.Assertions.assertEquals;

/// A [ThrowableHasher] shares one hash builder between all calls. Concurrent calls must still give the
/// same hashes as calls on one thread.
class ThrowableHasherConcurrencyTest {

    @Test
    void concurrentHashesAreCorrect() throws Exception {
        var throwables = IntStream.range(0, 20)
            .mapToObj(i -> i % 2 == 0
                ? new IllegalStateException("state " + i, new RuntimeException("cause " + i))
                : new IllegalArgumentException("argument " + i))
            .toList();
        var expected = throwables.stream()
            .map(throwable -> hasher().hash(throwable))
            .toList();

        var shared = hasher();
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(THREADS)) {
            var results = IntStream.range(0, THREADS)
                .mapToObj(thread -> executor.submit(() -> {
                    start.await();
                    return IntStream.range(0, ROUNDS)
                        .mapToObj(round -> (thread + round) % throwables.size())
                        .map(i -> Map.entry(i, shared.hash(throwables.get(i))))
                        .toList();
                }))
                .toList();
            start.countDown();
            for (var result : results) {
                for (var entry : result.get(60, TimeUnit.SECONDS)) {
                    assertEquals(expected.get(entry.getKey()), entry.getValue(), "hash of throwable " + entry.getKey());
                }
            }
        }
    }

    private static final int THREADS = 8;

    private static final int ROUNDS = 2000;

    private static ThrowableHasher<K128> hasher() {
        return new ThrowableHasher<>(true, HashBuilder.forKind(K128));
    }
}
