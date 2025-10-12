package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.hash.HashKind;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.kjetilv.uplift.edamame.impl.InternalFactory.create;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiThreadedTest {

    @SuppressWarnings("unchecked")
    @Test
    void test() {
        var mapsMemoizer = create(key -> CaKe.get(key.toString()), HashKind.K128, null);
        var complete = new AtomicBoolean();
        var voider = CompletableFuture.runAsync(() -> {
            for (var i = 0; i < 10; i++) {
                for (var j = 0; j < 100; j++) {
                    var caKeMap = map(i, j);
                    zz();
                    mapsMemoizer.putIfAbsent(i * 100 + j, caKeMap);
                }
            }
            mapsMemoizer.complete();
            complete.set(true);
        });

        var comparisons = 0;
        for (var x = 0; x < 5 && !complete.get(); x++) {
            for (var i = 0; i < 10; i++) {
                for (var j = 0; j < 100; j++) {
                    zz();
                    var map = mapsMemoizer.get(i * 100 + j);
                    if (map != null) {
                        comparisons++;
                        var cake = map(i, j);
                        assertEquals(cake.keySet(), map.keySet());
                        cake.keySet().forEach(key -> {
                            var cakeValue = (Map<CaKe, ?>) cake.get(key);
                            var mapValue = (Map<CaKe, ?>) map.get(key);
                            assertEquals(
                                new TreeMap<>(cakeValue).toString(),
                                new TreeMap<>(mapValue).toString(),
                                key::toString
                            );
                        });
                    }
                }
            }
        }
        voider.join();
        assertTrue(comparisons > 0);
    }

    private static void zz() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private static Map<CaKe, Object> map(int i, int j) {
        return Map.of(
            CaKe.get("foo-" + i + "-" + j),
            Map.of(
                "fooi", i,
                "fooj", j
            ),
            CaKe.get("foo-" + (i + j)),
            Map.of(
                "bar", i,
                "zot", j
            )
        );
    }
}
