package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.JsonSession;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class CachingJsonSessions {

    public static JsonSession create128() {
        return create(HashKind.K128);
    }

    public static JsonSession create256() {
        return create(HashKind.K256);
    }

    public static <H extends HashKind<H>> JsonSession create(H kind) {
        return create(kind, (Consumer<Object>) null);
    }

    public static <H extends HashKind<H>> JsonSession create(H kind, Consumer<Object> collisionHandler) {
        return create(kind, bi(collisionHandler));
    }

    public static <H extends HashKind<H>> JsonSession create(
        H kind,
        Consumer<Object> collisionHandler,
        CachingSettings cachingSettings
    ) {
        return create(kind, bi(collisionHandler), cachingSettings);
    }

    public static <H extends HashKind<H>> JsonSession create(H kind, BiConsumer<Hash<H>, Object> collisionHandler) {
        return create(kind, collisionHandler, null);
    }

    public static <H extends HashKind<H>> JsonSession create(H kind, CachingSettings cachingSettings) {
        return create(kind, (BiConsumer<Hash<H>, Object>) null, cachingSettings);
    }

    private CachingJsonSessions() {
    }

    private static <H extends HashKind<H>> BiConsumer<Hash<H>, Object> bi(Consumer<Object> collisionHandler) {
        return collisionHandler == null ? null : (_, item) -> collisionHandler.accept(item);
    }

    private static <H extends HashKind<H>> JsonSession create(
        H kind,
        BiConsumer<Hash<H>, Object> collisionHandler,
        CachingSettings cachingSettings
    ) {
        return new CachingJsonSession<>(kind, cachingSettings, collisionHandler);
    }
}
