package com.github.kjetilv.uplift.flambda;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

final class SyncPoint<K, V> {

    private final Lock lock = new ReentrantLock();

    private final Condition updated = lock.newCondition();

    private final Map<K, V> map = new HashMap<>();

    void put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        lock.lock();
        try {
            map.put(key, value);
        } finally {
            try {
                updated.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    V get(K key) {
        Objects.requireNonNull(key, "key");
        lock.lock();
        try {
            while (true) {
                var value = map.remove(key);
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
            lock.unlock();
        }
    }

    int size() {
        lock.lock();
        try {
            return map.size();
        } finally {
            lock.unlock();
        }
    }
}
