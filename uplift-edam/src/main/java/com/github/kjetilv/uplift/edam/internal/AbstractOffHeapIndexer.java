package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import module uplift.hash;
import module uplift.util;
import com.github.kjetilv.uplift.edam.HashFun;
import com.github.kjetilv.uplift.edam.Window;

import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static com.github.kjetilv.uplift.hash.HashKind.K256;

abstract sealed class AbstractOffHeapIndexer<K extends HashKind<K>> extends AbstractIndexer<K>
    permits OffHeapIndexer128, OffHeapIndexer256 {

    @SuppressWarnings("unchecked")
    static <K extends HashKind<K>> Indexer<Hash<K>> hashBuilder(
        Arena arena,
        Window window,
        HashBuilder<Bytes, K> hashBuilder,
        HashFun<Hash<?>> hashFun
    ) {
        return (AbstractOffHeapIndexer<K>) switch (hashBuilder.kind()) {
            case K128 _ -> new OffHeapIndexer128(arena, hashFun, window.count());
            case K256 _ -> new OffHeapIndexer256(arena, hashFun, window.count());
        };
    }

    AbstractOffHeapIndexer(HashFun<Hash<?>> hashFunction, long items) {
        super(hashFunction, items);
    }

    @Override
    protected abstract Slot<K> slot(long index);
}
