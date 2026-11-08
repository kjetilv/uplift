package com.github.kjetilv.uplift.edam.throwables;

import module java.base;
import com.github.kjetilv.uplift.edam.Handler;
import com.github.kjetilv.uplift.edam.HashFun;
import com.github.kjetilv.uplift.edam.Window;
import com.github.kjetilv.uplift.edam.internal.InternalFactory;
import com.github.kjetilv.uplift.edam.internal.Utils;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.util.Bytes;

@SuppressWarnings("unused")
public final class Throwables {

    private Throwables() {
    }

    private static final Window DEFAULT_WINDOW = new Window(Duration.ofMinutes(5), 64);

    private static final int DEFAULT_LENGTH = 3;

    public static final class OnHeap {

        public static <H extends HashKind<H>> Handler<Throwable, ThrowableInfo<H>, H> create(H kind) {
            return create(
                null,
                null,
                kind,
                null,
                -1,
                false
            );
        }

        public static <H extends HashKind<H>> Handler<Throwable, ThrowableInfo<H>, H> create(
            Window window,
            H kind,
            int maxLength,
            boolean messages
        ) {
            return create(
                null,
                window,
                kind,
                null,
                maxLength,
                messages
            );
        }

        public static <H extends HashKind<H>> Handler<Throwable, ThrowableInfo<H>, H> create(
            InstantSource now,
            Window window,
            H kind,
            HashBuilder<Bytes, H> hashBuilder,
            int maxLength,
            boolean messages
        ) {
            return InternalFactory.OnHeap.verified(
                now == null ? Utils.Time.UTC_NOW : now,
                window == null ? DEFAULT_WINDOW : window,
                hashBuilder == null ? HashBuilder.forKind(kind) : hashBuilder,
                maxLength < 0 ? DEFAULT_LENGTH : maxLength,
                messages
            );
        }

        private OnHeap() {
        }
    }

    public static final class OffHeap {

        public static <H extends HashKind<H>> Handler<Throwable, ThrowableInfo<H>, H> create(H kind) {
            return create(Arena.ofAuto(), kind);
        }

        public static <H extends HashKind<H>> Handler<Throwable, ThrowableInfo<H>, H> create(Arena arena, H kind) {
            return create(
                arena,
                null,
                null,
                kind,
                null,
                null,
                -1,
                false
            );
        }

        public static <H extends HashKind<H>> Handler<Throwable, ThrowableInfo<H>, H> create(
            Arena arena,
            Window window,
            H kind,
            int maxLength,
            boolean messages
        ) {
            return create(
                arena,
                null,
                window,
                kind,
                null,
                null,
                maxLength,
                messages
            );
        }

        public static <H extends HashKind<H>> Handler<Throwable, ThrowableInfo<H>, H> create(
            Arena arena,
            InstantSource now,
            Window window,
            H kind,
            HashBuilder<Bytes, H> hashBuilder,
            HashFun<Hash<?>> hashFun,
            int maxLength,
            boolean messages
        ) {
            return InternalFactory.OffHeap.verified(
                Objects.requireNonNull(arena, "arena"),
                now == null ? Utils.Time.UTC_NOW : now,
                window == null ? DEFAULT_WINDOW : window,
                hashBuilder == null ? HashBuilder.forKind(kind) : hashBuilder,
                hashFun == null ? InternalFactory.JAVA : hashFun,
                maxLength < 0 ? DEFAULT_LENGTH : maxLength,
                messages
            );
        }

        private OffHeap() {
        }
    }
}
