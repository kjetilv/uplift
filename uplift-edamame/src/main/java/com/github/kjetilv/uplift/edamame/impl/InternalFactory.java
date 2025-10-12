package com.github.kjetilv.uplift.edamame.impl;

import module uplift.edamame;
import module uplift.hash;

import static java.util.Objects.requireNonNull;

public final class InternalFactory {

    /// @param <I>        Id type
    /// @param <K>        Key type
    /// @param keyHandler Key handler, null means default behaviour
    /// @return Map memoizer
    public static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(
        KeyHandler<K> keyHandler,
        H kind
    ) {
        return create(keyHandler, kind, null);
    }

    /// @param keyHandler Key handler, null means default behaviour
    /// @param pojoBytes  Pojo bytes
    /// @return Map memoizer
    public static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(
        KeyHandler<K> keyHandler,
        H kind,
        PojoBytes pojoBytes
    ) {
        return create(keyHandler, null, kind, pojoBytes);
    }

    /// @param pojoBytes Pojo bytes
    /// @return Map memoizer
    public static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(
        H kind,
        PojoBytes pojoBytes
    ) {
        return create(null, null, kind, pojoBytes);
    }

    /// @param <I>        Id type
    /// @param <K>        Key type
    /// @param <H>        Hash kind
    /// @param keyHandler Key handler, null means default behaviour
    /// @param leafHasher Leaf hasher, for testing purposes
    /// @return Map memoizer
    public static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(
        KeyHandler<K> keyHandler,
        LeafHasher<H> leafHasher,
        H kind,
        PojoBytes pojoBytes
    ) {
        requireNonNull(kind, "kind");
        var treeHasher = mapHasher(kind, keyHandler, leafHasher, pojoBytes);
        var canonicalValues = CanonicalSubstructuresCataloguer.<K, H>create();
        return new MapsMemoizerImpl<>(treeHasher, canonicalValues);
    }

    public static <H extends HashKind<H>> LeafHasher<H> leafHasher(H kind, PojoBytes pojoBytes) {
        return LeafHasher.create(kind, pojoBytes);
    }

    public static <K, H extends HashKind<H>> Canonicalizer<K, H> canonicalizer() {
        return canonicalizer(false);
    }

    public static <K, H extends HashKind<H>> Canonicalizer<K, H> canonicalizer(boolean collisionsNeverHappen) {
        return new CanonicalSubstructuresCataloguer<>(collisionsNeverHappen);
    }

    private InternalFactory() {
    }

    private static <K, H extends HashKind<H>> TreeHasher<K, H> mapHasher(
        H kind,
        KeyHandler<K> keyHandler,
        LeafHasher<H> leafHasher,
        PojoBytes pojoBytes
    ) {
        requireNonNull(kind, "kind");
        KeyHandler<K> canonicalKeys = new CanonicalKeysCataloguer<>(
            keyHandler != null
                ? keyHandler
                : KeyHandler.defaultHandler());
        var hasher = leafHasher != null ? leafHasher : leafHasher(kind, pojoBytes);
        return new RecursiveTreeHasher<>(
            () -> Hashes.hashBuilder(kind),
            canonicalKeys,
            requireNonNull(hasher, "leafHasher"),
            kind
        );
    }
}
