package com.github.kjetilv.uplift.edam.throwables;

import module java.base;
import module uplift.hash;
import module uplift.util;
import com.github.kjetilv.uplift.edam.Handler;
import com.github.kjetilv.uplift.edam.HashFun;
import com.github.kjetilv.uplift.edam.Window;
import com.github.kjetilv.uplift.edam.internal.InternalFactory;
import com.github.kjetilv.uplift.edam.internal.Utils;

@SuppressWarnings("unused")
public final class Throwables {

    public static <K extends HashKind<K>> Handler<Throwable, ThrowableInfo<K>, K> onHeap(K kind) {
        return onHeap(
            null,
            null,
            kind,
            null,
            -1,
            false
        );
    }

    public static <K extends HashKind<K>> Handler<Throwable, ThrowableInfo<K>, K> onHeap(
        Window window,
        K kind,
        int maxLength,
        boolean messages
    ) {
        return onHeap(
            null,
            window,
            kind,
            null,
            maxLength,
            messages
        );
    }

    public static <K extends HashKind<K>> Handler<Throwable, ThrowableInfo<K>, K> onHeap(
        Supplier<Instant> now,
        Window window,
        K kind,
        HashBuilder<Bytes, K> hashBuilder,
        int maxLength,
        boolean messages
    ) {
        return InternalFactory.createVerifiedOnHeap(
            now == null ? Utils.Time.UTC_NOW : now,
            window == null ? DEFAULT_WINDOW : window,
            hashBuilder == null ? Hashes.hashBuilder(kind) : hashBuilder,
            maxLength < 0 ? DEFAULT_LENGTH : maxLength,
            messages
        );
    }

    public static <K extends HashKind<K>> Handler<Throwable, ThrowableInfo<K>, K> offHeap(
        Arena arena,
        K kind
    ) {
        return offHeap(
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

    public static <K extends HashKind<K>> Handler<Throwable, ThrowableInfo<K>, K> offHeap(
        Arena arena,
        Window window,
        K kind,
        int maxLength,
        boolean messages
    ) {
        return offHeap(
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

    public static <K extends HashKind<K>> Handler<Throwable, ThrowableInfo<K>, K> offHeap(
        Arena arena,
        Supplier<Instant> now,
        Window window,
        K kind,
        HashBuilder<Bytes, K> hashBuilder,
        HashFun<Hash<?>> hashFun,
        int maxLength,
        boolean messages
    ) {
        return InternalFactory.createVerifiedOffHeap(
            Objects.requireNonNull(arena, "arena"),
            now == null ? Utils.Time.UTC_NOW : now,
            window == null ? DEFAULT_WINDOW : window,
            hashBuilder == null ? Hashes.hashBuilder(kind) : hashBuilder,
            hashFun == null ? InternalFactory.JAVA : hashFun,
            maxLength < 0 ? DEFAULT_LENGTH : maxLength,
            messages
        );
    }

    private Throwables() {
    }

    private static final Window DEFAULT_WINDOW = new Window(Duration.ofMinutes(5), 64);

    private static final int DEFAULT_LENGTH = 3;
}
