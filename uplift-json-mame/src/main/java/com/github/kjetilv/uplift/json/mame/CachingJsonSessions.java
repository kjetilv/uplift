package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.JsonSession;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class CachingJsonSessions {

    public static <H extends HashKind<H>> JsonSession create(H kind) {
        return create(kind, (Consumer<Object>) null);
    }

    public static <H extends HashKind<H>> JsonSession create(H kind, Consumer<Object> collisionHandler) {
        return create(
            kind,
            collisionHandler == null
                ? null
                : (_, item) -> collisionHandler.accept(item)
        );
    }

    public static <H extends HashKind<H>> JsonSession create(H kind, BiConsumer<Hash<H>, Object> collisionHandler) {
        return create(kind, collisionHandler, false, false);
    }

    public static <H extends HashKind<H>> JsonSession create(H kind, boolean collisionsNeverHappen) {
        return create(kind, null, false, collisionsNeverHappen);
    }

    private CachingJsonSessions() {
    }

    private static <H extends HashKind<H>> JsonSession create(
        H kind,
        BiConsumer<Hash<H>, Object> collisionHandler,
        boolean preserveNulls,
        boolean collisionsNeverHappen
    ) {
        return new CachingJsonSession<>(kind, preserveNulls, collisionsNeverHappen, collisionHandler);
    }
}
