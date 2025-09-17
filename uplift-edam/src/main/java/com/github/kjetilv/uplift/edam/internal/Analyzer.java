package com.github.kjetilv.uplift.edam.internal;

import com.github.kjetilv.uplift.edam.Analysis;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

final class Analyzer<T, K extends HashKind<K>> {

    private final Supplier<Instant> now;

    private final Lock lock = new ReentrantLock();

    private final Hasher<T, K> hasher;

    private SequenceTracker<K> sequenceTracker;

    Analyzer(Hasher<T, K> hasher, Storage<K> storage, Supplier<Instant> now, int maxLength) {
        this.hasher = Objects.requireNonNull(hasher, "ider");
        this.now = Objects.requireNonNull(now, "now");
        this.sequenceTracker = new SequenceTracker<>(storage, new Detector(maxLength));
    }

    Analysis<K> analyze(T item) {
        Instant now = this.now.get();
        Hash<K> hash = hasher.hash(item);
        Occurrence<K> occ = new Occurrence<>(now, hash);
        return updatedState(occ).process(occ);
    }

    @SuppressWarnings("DataFlowIssue")
    private SequenceTracker<K> updatedState(Occurrence<K> occ) {
        try {
            lock.lock();
            return this.sequenceTracker = sequenceTracker.update(occ);
        } finally {
            lock.unlock();
        }
    }
}
