package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.*;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.hash.Hashes;

import static java.util.Objects.requireNonNull;

public final class InternalFactory {

    /**
     * @param <I>        Id type
     * @param <K>        Key type
     * @param keyHandler Key handler, null means default behaviour
     * @return Map memoizer
     */
    public static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(
        KeyHandler<K> keyHandler,
        H kind
    ) {
        return create(keyHandler, kind, null);
    }

    /**
     * @param keyHandler Key handler, null means default behaviour
     * @param pojoBytes  Pojo bytes
     * @return Map memoizer
     */
    public static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(
        KeyHandler<K> keyHandler,
        H kind,
        PojoBytes pojoBytes
    ) {
        return create(keyHandler, null, kind, pojoBytes);
    }

    /**
     * @param pojoBytes Pojo bytes
     * @return Map memoizer
     */
    public static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(
        H kind,
        PojoBytes pojoBytes
    ) {
        return create(null, null, kind, pojoBytes);
    }

    /**
     * @param <I>        Id type
     * @param <K>        Key type
     * @param <H>        Hash kind
     * @param keyHandler Key handler, null means default behaviour
     * @param leafHasher Leaf hasher, for testing purposes
     * @return Map memoizer
     */
    public static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(
        KeyHandler<K> keyHandler,
        LeafHasher<H> leafHasher,
        H kind,
        PojoBytes pojoBytes
    ) {
        requireNonNull(kind, "kind");
        MapHasher<K, H> mapHasher = mapHasher(kind, keyHandler, leafHasher, pojoBytes);
        Canonicalizer<K, H> canonicalValues = new CanonicalSubstructuresCataloguer<>();
        return new MapsMemoizerImpl<>(mapHasher, canonicalValues);
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

    private static <K, H extends HashKind<H>> MapHasher<K, H> mapHasher(
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
        LeafHasher<H> hasher = leafHasher != null
            ? leafHasher
            : leafHasher(kind, pojoBytes == null ? PojoBytes.HASHCODE : pojoBytes);
        return new RecursiveTreeHasher<>(
            requireNonNull(() -> Hashes.hashBuilder(kind), "newBuilder"),
            canonicalKeys,
            requireNonNull(hasher, "leafHasher"),
            kind
        );
    }
}
