package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import module uplift.edam;
import module uplift.hash;
import module uplift.util;

public final class InternalFactory {

    public static final HashFun<Hash<?>> JAVA = Object::hashCode;

    public static <K extends HashKind<K>> Handler<Throwable, ThrowableInfo<K>, K> createVerifiedOnHeap(
        Supplier<Instant> now,
        Window window,
        HashBuilder<Bytes, K> hashBuilder,
        int maxLength,
        boolean messages
    ) {
        return new DefaultHandler<>(
            new Analyzer<>(
                new ThrowableHasher<>(
                    messages,
                    Objects.requireNonNull(hashBuilder, "hashBuilder")
                ),
                new OnHeapStorage<>(Objects.requireNonNull(window, "window")),
                Objects.requireNonNull(now, "now"),
                maxLength
            ),
            new ThrowableInfoProvider<>()
        );
    }

    public static <K extends HashKind<K>> Handler<Throwable, ThrowableInfo<K>, K> createVerifiedOffHeap(
        Arena arena,
        Supplier<Instant> now,
        Window window,
        HashBuilder<Bytes, K> hashBuilder,
        HashFun<Hash<?>> hashFun,
        int maxLength,
        boolean messages
    ) {
        Indexer<Hash<K>> indexer = AbstractOffHeapIndexer.hashBuilder(
            Objects.requireNonNull(arena, "arena"),
            Objects.requireNonNull(window, "window"),
            Objects.requireNonNull(hashBuilder, "hashBuilder"),
            Objects.requireNonNull(hashFun, "hashFun")
        );
        return new DefaultHandler<>(
            new Analyzer<>(
                new ThrowableHasher<>(messages, hashBuilder),
                new OffHeapStorage<>(window, indexer, arena),
                Objects.requireNonNull(now, "now"),
                maxLength
            ),
            new ThrowableInfoProvider<>()
        );
    }

    private InternalFactory() {
    }
}
