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
        return create(kind, collisionHandler, null);
    }

    public static <H extends HashKind<H>> JsonSession create(H kind, CachingSettings cachingSettings) {
        return create(kind, null, cachingSettings);
    }

    private CachingJsonSessions() {
    }

    private static <H extends HashKind<H>> JsonSession create(
        H kind,
        BiConsumer<Hash<H>, Object> collisionHandler,
        CachingSettings cachingSettings
    ) {
        return new CachingJsonSession<>(kind, cachingSettings, collisionHandler);
    }
}
