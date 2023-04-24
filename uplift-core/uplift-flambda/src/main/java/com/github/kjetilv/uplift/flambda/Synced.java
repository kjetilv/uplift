package com.github.kjetilv.uplift.flambda;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

final class Synced<K, V> {

    private final Lock lock = new ReentrantLock();

    private final Condition updated = lock.newCondition();

    private final Map<? super K, V> map;

    private final AtomicLong waiters = new AtomicLong();

    Synced(Map<? super K, V> map) {
        this.map = Objects.requireNonNull(map, "map");
    }

    void put(K key, V value) {
        lock.lock();
        try {
            map.put(key, value);
        } finally {
            updated.signalAll();
            lock.unlock();
        }
    }

    V get(K key) {
        waiters.incrementAndGet();
        lock.lock();
        try {
            while (true) {
                V value = map.get(key);
                if (value != null) {
                    return value;
                }
                try {
                    updated.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted", e);
                }
            }
        } finally {
            waiters.decrementAndGet();
            lock.unlock();
        }
    }
}
