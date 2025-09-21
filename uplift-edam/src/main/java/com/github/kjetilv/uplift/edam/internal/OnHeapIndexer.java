package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import module jdk.incubator.vector;
import module uplift.edam;
import module uplift.hash;
import module uplift.util;

@SuppressWarnings("unused")
final class OnHeapIndexer<K extends HashKind<K>> extends AbstractIndexer<K> {

    private final Hash<K>[] hashes;

    private final K kind;

    OnHeapIndexer(HashFun<Hash<?>> hashFunction, K kind, long count) {
        super(hashFunction, count);
        this.kind = kind;
        int size = toInt(hashFunction.slotCount(count));
        //noinspection unchecked
        this.hashes = new Hash[size];
    }

    @Override
    protected Slot<K> slot(long index) {
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
            Hash<K> hash = hashes[index];
            return hash == null ? kind.blank() : hash;
        }

        @Override
        public void store(Hash<K> hash) {
            hashes[index] = hash;
        }
    }
}
