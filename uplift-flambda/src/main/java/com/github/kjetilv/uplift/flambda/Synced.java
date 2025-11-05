package com.github.kjetilv.uplift.flambda;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

final class Synced<K, V> {

    private final Lock lock = new ReentrantLock();

    private final Condition updated = lock.newCondition();

    private final Map<? super K, V> map = new ConcurrentHashMap<>();

    private final AtomicLong waiters = new AtomicLong();

    void put(K key, V value) {
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
        waiters.incrementAndGet();
        lock.lock();
        try {
            while (true) {
                var value = map.get(key);
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
            try {
                waiters.decrementAndGet();
            } finally {
                lock.unlock();
            }
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
