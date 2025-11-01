package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.edam.HashFun;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

abstract sealed class AbstractIndexer<K extends HashKind<K>> implements Indexer<Hash<K>>
    permits AbstractOffHeapIndexer, OnHeapIndexer {

    private final long limit;

    private final long lastIndex;

    private final HashFun<Hash<?>> hashFunction;

    private long count;

    AbstractIndexer(HashFun<Hash<?>> hashFunction, long items) {
        this.hashFunction = Objects.requireNonNull(hashFunction, "hashFunction");
        this.limit = this.hashFunction.slotCount(items);
        this.lastIndex = this.limit - 1;
    }

    @Override
    public final long exchange(Hash<K> hash) {
        var initPos = hashFunction.compute(hash) & lastIndex;
        var pos = initPos;
        while (true) {
            var slot = slot(pos);
            var loaded = slot.load();
            if (loaded.isBlank()) {
                try {
                    slot.store(hash);
                    return pos;
                } finally {
                    count++;
                }
            }
            if (hash.equals(loaded)) {
                return pos;
            }
            pos = pos + 1 & lastIndex;
            if (pos == initPos) {
                throw new IllegalStateException(this + " could not find free slot for " + hash);
            }
        }
    }

    @Override
    public final Hash<K> exchange(long index) {
        if (index == limit) {
            throw new IllegalArgumentException(this + ": Index out of bounds: " + index);
        }
        var pos = index & lastIndex;
        var slot = slot(pos);
        var hash = slot.load();
        if (hash.isBlank()) {
            throw new IllegalArgumentException(this + ": No hash @ " + index);
        }
        return hash;
    }

    @Override
    public final long limit() {
        return limit;
    }

    protected abstract Slot<K> slot(long index);

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + count + "/" + limit + "]";
    }

    protected sealed interface Slot<K extends HashKind<K>> permits
        OffHeapIndexer128.SegmentSlot128,
        OffHeapIndexer256.SegmentSlot256,
        OnHeapIndexer.IndexedSlot {

        Hash<K> load();

        void store(Hash<K> hash);
    }
}
