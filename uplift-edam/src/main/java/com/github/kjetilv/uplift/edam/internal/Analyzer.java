package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.edam.Analysis;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.hash.HashKind;

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
        var now = this.now.get();
        var hash = hasher.hash(item);
        var occurrence = new Occurrence<K>(now, hash);
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
