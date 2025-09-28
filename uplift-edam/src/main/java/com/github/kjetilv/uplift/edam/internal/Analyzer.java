package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import module uplift.edam;
import module uplift.hash;

final class Analyzer<T, K extends HashKind<K>> {

    private final Supplier<Instant> now;

    private final Lock lock = new ReentrantLock();

    private final Hasher<T, K> hasher;

    private SequenceTracker<K> sequenceTracker;

    Analyzer(Hasher<T, K> hasher, Storage<K> storage, Supplier<Instant> now, int maxLength) {
        this.hasher = Objects.requireNonNull(hasher, "hasher");
        this.now = Objects.requireNonNull(now, "now");
        this.sequenceTracker = new SequenceTracker<>(storage, new Detector(maxLength));
    }

    Analysis<K> analyze(T item) {
        Instant now = this.now.get();
        Hash<K> hash = hasher.hash(item);
        Occurrence<K> occurrence = new Occurrence<>(now, hash);
        return updatedState(occurrence).process(occurrence);
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
