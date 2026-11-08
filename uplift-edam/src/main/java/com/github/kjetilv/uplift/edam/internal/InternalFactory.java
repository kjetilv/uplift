package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.edam.Handler;
import com.github.kjetilv.uplift.edam.HashFun;
import com.github.kjetilv.uplift.edam.Window;
import com.github.kjetilv.uplift.edam.throwables.ThrowableInfo;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.util.Bytes;

public final class InternalFactory {

    public static final HashFun<Hash<?>> JAVA = Object::hashCode;

    private InternalFactory() {
    }

    public static final class OffHeap {

        public static <H extends HashKind<H>> Handler<Throwable, ThrowableInfo<H>, H> verified(
            Arena arena,
            InstantSource now,
            Window window,
            HashBuilder<Bytes, H> hashBuilder,
            HashFun<Hash<?>> hashFun,
            int maxLength,
            boolean messages
        ) {
            var indexer = AbstractOffHeapIndexer.hashBuilder(
                Objects.requireNonNull(arena, "arena"),
                Objects.requireNonNull(window, "window"),
                Objects.requireNonNull(hashBuilder, "hashBuilder"),
                Objects.requireNonNull(hashFun, "hashFun")
            );
            return new DefaultHandler<>(
                Analyzers.analyzer(
                    new ThrowableHasher<>(messages, hashBuilder),
                    new OffHeapStorage<>(window, indexer, arena),
                    Objects.requireNonNull(now, "now"),
                    maxLength
                ),
                new ThrowableInfoProvider<>()
            );
        }

        private OffHeap() {
        }
    }

    public static final class OnHeap {

        public static <H extends HashKind<H>> Handler<Throwable, ThrowableInfo<H>, H> verified(
            InstantSource now,
            Window window,
            HashBuilder<Bytes, H> hashBuilder,
            int maxLength,
            boolean messages
        ) {
            return new DefaultHandler<>(
                Analyzers.analyzer(
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

        private OnHeap() {
        }
    }
}
