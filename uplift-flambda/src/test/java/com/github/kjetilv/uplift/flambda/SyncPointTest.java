package com.github.kjetilv.uplift.flambda;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SyncPointTest {

    @Test
    void putThenGet() {
        var syncPoint = new SyncPoint<String, String>();
        syncPoint.put("key", "value");
        assertEquals(1, syncPoint.size());
        assertEquals("value", syncPoint.get("key"));
        assertEquals(0, syncPoint.size()); // Entry removed after get
    }

    @Test
    void getThenPut() throws Exception {
        var syncPoint = new SyncPoint<String, String>();
        var result = new AtomicReference<String>();
        var getLatch = new CountDownLatch(1);
        var startedLatch = new CountDownLatch(1);

        var getter = new Thread(() -> {
            startedLatch.countDown();
            result.set(syncPoint.get("key"));
            getLatch.countDown();
        });
        getter.start();

        assertTrue(startedLatch.await(1, TimeUnit.SECONDS));
        Thread.sleep(50); // Allow getter to block on get()

        assertFalse(getLatch.await(50, TimeUnit.MILLISECONDS), "get() should block");

        syncPoint.put("key", "value");

        assertTrue(getLatch.await(1, TimeUnit.SECONDS), "get() should return after put()");
        assertEquals("value", result.get());
    }

    @Test
    void multipleKeys() throws Exception {
        var syncPoint = new SyncPoint<String, Integer>();
        var result1 = new AtomicReference<Integer>();
        var result2 = new AtomicReference<Integer>();
        var latch = new CountDownLatch(2);

        new Thread(() -> {
            result1.set(syncPoint.get("one"));
            latch.countDown();
        }).start();

        new Thread(() -> {
            result2.set(syncPoint.get("two"));
            latch.countDown();
        }).start();

        Thread.sleep(50);
        syncPoint.put("one", 1);
        syncPoint.put("two", 2);

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(1, result1.get());
        assertEquals(2, result2.get());
        assertEquals(0, syncPoint.size()); // Entries removed after get
    }

    @Test
    void consumeOnce() throws Exception {
        var syncPoint = new SyncPoint<String, String>();
        var result = new AtomicReference<String>();
        var latch = new CountDownLatch(1);

        new Thread(() -> {
            result.set(syncPoint.get("key"));
            latch.countDown();
        }).start();

        Thread.sleep(50);
        syncPoint.put("key", "first");

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals("first", result.get());
        assertEquals(0, syncPoint.size());

        // Second get for same key should block until another put
        var result2 = new AtomicReference<String>();
        var latch2 = new CountDownLatch(1);

        new Thread(() -> {
            result2.set(syncPoint.get("key"));
            latch2.countDown();
        }).start();

        Thread.sleep(50);
        assertFalse(latch2.await(50, TimeUnit.MILLISECONDS), "Second get should block");

        syncPoint.put("key", "second");
        assertTrue(latch2.await(1, TimeUnit.SECONDS));
        assertEquals("second", result2.get());
    }

    @Test
    void concurrentPutsAndGets() throws Exception {
        var syncPoint = new SyncPoint<Integer, Integer>();
        try (var executor = Executors.newFixedThreadPool(10)) {
            var count = 100;
            var latch = new CountDownLatch(count * 2);

            for (int i = 0; i < count; i++) {
                var key = i;
                executor.submit(() -> {
                    syncPoint.put(key, key * 2);
                    latch.countDown();
                });
                executor.submit(() -> {
                    var value = syncPoint.get(key);
                    assertEquals(key * 2, value);
                    latch.countDown();
                });
            }

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(0, syncPoint.size()); // All entries consumed
            executor.shutdown();
        }
    }
}