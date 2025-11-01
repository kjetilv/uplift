package com.github.kjetilv.uplift.edam.internal;

import com.github.kjetilv.uplift.edam.Window;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unchecked")
class OffHeapStorageTest {

    @Test
    void ring() {
        try (
            var arena = Arena.ofConfined()
        ) {
            var storage = OffHeapStorage.create(
                new Window(Duration.ofDays(1), 3),
                new OffHeapIndexer128(arena, Object::hashCode, 3),
                arena
            );
            Hash<K128>[] hs = IntStream.range(0, 10)
                .mapToObj(_ -> K128.random())
                .toArray(Hash[]::new);
            var instant = new AtomicReference<Instant>(Instant.EPOCH);
            Supplier<Instant> now = () -> instant.updateAndGet(i -> i.plusSeconds(1));

            Occurrence<K128>[] occs = Arrays.stream(hs)
                .map(hash -> new Occurrence<>(now.get(), hash))
                .toArray(Occurrence[]::new);

            storage.store(occs[0]);
            assertEquals(hs[0], storage.get(0).hash());

            storage.store(occs[1]);
            assertEquals(occs[0].hash(), storage.get(0).hash());
            assertEquals(hs[1], storage.get(1).hash());

            assertEquals(
                Arrays.asList(hs).subList(0, 2),
                storage.forward().spool()
                    .map(Occurrence::hash)
                    .toList(),
                () -> print(hs)
            );

            storage.store(occs[2]);
            assertEquals(hs[2], storage.get(2).hash());

            assertEquals(
                Arrays.asList(hs).subList(0, 3),
                storage.forward().spool()
                    .map(Occurrence::hash)
                    .toList(),
                print(hs)
            );

            storage.store(occs[3]);
            assertEquals(hs[3], storage.get(2).hash());

            assertEquals(
                Arrays.asList(hs).subList(1, 4),
                storage.forward().spool()
                    .map(Occurrence::hash)
                    .toList()
            );
            assertEquals(
                reverse(Arrays.asList(hs).subList(1, 4)),
                storage.rewind().spool()
                    .map(Occurrence::hash)
                    .toList()
            );

            var o2Time = storage.forward().spool(occs[2].time())
                .toList();
            assertEquals(1, o2Time.size());
            assertEquals(occs[1], o2Time.getFirst());

            var o3Time = storage.forward().spool(occs[3].time())
                .toList();
            assertEquals(2, o3Time.size());
            assertEquals(occs[1], o3Time.getFirst());
            assertEquals(occs[2], o3Time.getLast());

            var o3TimeDown = storage.rewind().spool(occs[3].time())
                .toList();
            assertEquals(1, o3TimeDown.size());
            assertEquals(occs[3], o3TimeDown.getFirst());

            var o2TimeDown = storage.rewind().spool(occs[2].time())
                .toList();
            assertEquals(2, o2TimeDown.size());
            assertEquals(occs[3], o2TimeDown.getFirst());
            assertEquals(occs[2], o2TimeDown.getLast());
        }
    }

    @Test
    void store() {
        try (
            var arena = Arena.ofConfined()
        ) {
            var storage = OffHeapStorage.create(
                new Window(Duration.ofDays(1), 10),
                new OffHeapIndexer128(arena, Object::hashCode, 10),
                arena
            );
            var occurrenceA = new Occurrence<K128>(Instant.now(), HashKind.K128.random());
            var occurrenceB = new Occurrence<K128>(Instant.now().minusSeconds(1), HashKind.K128.random());
            storage.store(occurrenceA);
            assertEquals(occurrenceA, storage.get(0));

            storage.store(occurrenceB);
            assertEquals(occurrenceA, storage.get(0));
            assertEquals(occurrenceB, storage.get(1));

            var cursor1 = storage.rewind();
            assertEquals(occurrenceB, cursor1.next().get());
            assertEquals(occurrenceA, cursor1.next().get());
            assertTrue(cursor1.next().isEmpty());

            var cursor2 = storage.forward();
            assertEquals(occurrenceA, cursor2.next().get());
            assertEquals(occurrenceB, cursor2.next().get());
            assertTrue(cursor2.next().isEmpty());

            var aCursor = storage.rewind(occurrenceA.hash());
            assertEquals(occurrenceA, aCursor.next().get());
            assertTrue(aCursor.next().isEmpty());

            var list = storage.rewind().spool().limit(1)
                .toList();
            assertEquals(1, list.size());
            assertEquals(occurrenceB, list.getFirst());

            var rew = storage.rewind();
            var list2 = rew.spool()
                .toList();
            assertEquals(2, list2.size());
            assertEquals(occurrenceB, list2.getFirst());
            assertEquals(occurrenceA, list2.getLast());

            var list3 = storage.forward().spool()
                .toList();
            assertEquals(2, list3.size());
            assertEquals(occurrenceA, list3.getFirst());
            assertEquals(occurrenceB, list3.getLast());
        }
    }

    private static <T> List<T> reverse(List<T> hashes) {
        var copy = new ArrayList<T>(hashes);
        Collections.reverse(copy);
        return List.copyOf(copy);
    }

    private static String print(Hash<?>[] hs) {
        return IntStream.range(0, hs.length)
            .mapToObj(i -> i + ":" + hs[i])
            .collect(Collectors.joining(", "));
    }
}