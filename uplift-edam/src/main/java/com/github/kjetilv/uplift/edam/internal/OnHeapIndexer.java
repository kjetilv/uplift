package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import module jdk.incubator.vector;
import com.github.kjetilv.uplift.edam.HashFun;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

@SuppressWarnings("unused")
final class OnHeapIndexer<H extends HashKind<H>> extends AbstractIndexer<H> {

    private final Hash<H>[] hashes;

    private final H kind;

    OnHeapIndexer(HashFun<Hash<?>> hashFunction, H kind, long count) {
        super(hashFunction, count);
        this.kind = kind;
        var size = toInt(hashFunction.slotCount(count));
        //noinspection unchecked
        this.hashes = new Hash[size];
    }

    @Override
    protected Slot<H> slot(long index) {
        return new IndexedSlot<>(toInt(index), hashes, kind);
    }

    private static int toInt(long index) {
        try {
            return Math.toIntExact(index);
        } catch (Exception e) {
            throw new IllegalArgumentException("Mis-sized array: " + index, e);
        }
    }

    record IndexedSlot<K extends HashKind<K>>(int index, Hash<K>[] hashes, K kind)
        implements Slot<K> {

        @Override
        public Hash<K> load() {
            var hash = hashes[index];
            return hash == null ? kind.blank() : hash;
        }

        @Override
        public void store(Hash<K> hash) {
            hashes[index] = hash;
        }
    }
}
