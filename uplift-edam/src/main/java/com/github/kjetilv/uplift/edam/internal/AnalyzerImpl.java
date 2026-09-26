package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.edam.Analysis;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.hash.HashKind;

final class AnalyzerImpl<T, H extends HashKind<H>> implements Analyzer<T, H> {

    private final InstantSource now;

    private final Lock lock = new ReentrantLock();

    private final Hasher<T, H> hasher;

    private SequenceTracker<H> sequenceTracker;

    AnalyzerImpl(Hasher<T, H> hasher, Storage<H> storage, InstantSource now, int maxLength) {
        this.hasher = Objects.requireNonNull(hasher, "hasher");
        this.now = Objects.requireNonNull(now, "now");
        this.sequenceTracker = new SequenceTracker<>(storage, new Detector(maxLength));
    }

    @Override
    public Analysis<H> analyze(T item) {
        var now = this.now.instant();
        var hash = hasher.hash(item);
        var occurrence = new Occurrence<>(now, hash);
        return updatedState(occurrence).process(occurrence);
    }

    @SuppressWarnings("DataFlowIssue")
    private SequenceTracker<H> updatedState(Occurrence<H> occ) {
        try {
            lock.lock();
            return this.sequenceTracker = sequenceTracker.update(occ);
        } finally {
            lock.unlock();
        }
    }
}
