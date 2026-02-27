package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.*;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;

import static java.util.Objects.requireNonNull;

public final class InternalFactory {

    /// @param <I>        Id type
    /// @param <MK>        Key type
    /// @param keyHandler Key handler, null means default behaviour
    /// @return Map memoizer
    public static <I, MK, K extends HashKind<K>> MapsMemoizer<I, MK> create(
        KeyHandler<MK> keyHandler,
        K kind
    ) {
        return create(keyHandler, kind, null);
    }

    /// @param keyHandler Key handler, null means default behaviour
    /// @param pojoBytes  Pojo bytes
    /// @return Map memoizer
    public static <I, MK, K extends HashKind<K>> MapsMemoizer<I, MK> create(
        KeyHandler<MK> keyHandler,
        K kind,
        PojoBytes pojoBytes
    ) {
        return create(keyHandler, null, kind, pojoBytes);
    }

    /// @param pojoBytes Pojo bytes
    /// @return Map memoizer
    public static <I, MK, K extends HashKind<K>> MapsMemoizer<I, MK> create(
        K kind,
        PojoBytes pojoBytes
    ) {
        return create(null, null, kind, pojoBytes);
    }

    /// @param <I>        Id type
    /// @param <MK>        Key type
    /// @param <K>        Hash kind
    /// @param keyHandler Key handler, null means default behaviour
    /// @param leafHasher Leaf hasher, for testing purposes
    /// @return Map memoizer
    public static <I, MK, K extends HashKind<K>> MapsMemoizer<I, MK> create(
        KeyHandler<MK> keyHandler,
        LeafHasher<K> leafHasher,
        K kind,
        PojoBytes pojoBytes
    ) {
        requireNonNull(kind, "kind");
        var treeHasher = mapHasher(kind, keyHandler, leafHasher, pojoBytes);
        Canonicalizer<MK, K> canonicalValues = CanonicalSubstructuresCataloguer.create();
        return new MapsMemoizerImpl<>(treeHasher, canonicalValues);
    }

    public static <K extends HashKind<K>> LeafHasher<K> leafHasher(K kind, PojoBytes pojoBytes) {
        return LeafHasher.create(kind, pojoBytes);
    }

    public static <MK, K extends HashKind<K>> Canonicalizer<MK, K> canonicalizer() {
        return canonicalizer(false);
    }

    public static <MK, K extends HashKind<K>> Canonicalizer<MK, K> canonicalizer(boolean collisionsNeverHappen) {
        return new CanonicalSubstructuresCataloguer<>(collisionsNeverHappen);
    }

    private InternalFactory() {
    }

    private static <MK, K extends HashKind<K>> TreeHasher<MK, K> mapHasher(
        K kind,
        KeyHandler<MK> keyHandler,
        LeafHasher<K> leafHasher,
        PojoBytes pojoBytes
    ) {
        requireNonNull(kind, "kind");
        return new RecursiveTreeHasher<>(
            () -> HashBuilder.forKind(kind),
            getCanonicalKeys(keyHandler),
            leafHasher == null ? leafHasher(kind, pojoBytes) : leafHasher,
            kind
        );
    }

    private static <K> KeyHandler<K> getCanonicalKeys(KeyHandler<K> handler) {
        return new CanonicalKeysCataloguer<>(
            handler == null ? KeyHandler.defaultHandler() : handler
        );
    }
}
