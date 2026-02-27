package com.github.kjetilv.uplift.edamame.impl;

import module java.base;
import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.edamame.MapsMemoizers;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.util.Bytes;

import static com.github.kjetilv.uplift.edamame.HashedTree.*;
import static com.github.kjetilv.uplift.util.Collectioons.*;
import static com.github.kjetilv.uplift.util.Maps.sizedMap;
import static java.util.Objects.requireNonNull;

/// Normalizes input trees and builds [hashed trees][HashedTree]. Stateless and thread-safe.
///
/// @param <I> Identifier type
record RecursiveTreeHasher<I, K extends HashKind<K>>(
    Supplier<HashBuilder<Bytes, K>> newBuilder,
    KeyHandler<I> keyHandler,
    LeafHasher<K> leafHasher,
    K kind
) implements TreeHasher<I, K> {

    /// @param newBuilder Hash builder, not null
    /// @param keyHandler Key handler, not null
    /// @param leafHasher Hasher, not null
    /// @see MapsMemoizers#create(KeyHandler, HashKind)
    RecursiveTreeHasher(
        Supplier<HashBuilder<Bytes, K>> newBuilder,
        KeyHandler<I> keyHandler,
        LeafHasher<K> leafHasher,
        K kind
    ) {
        this.newBuilder = requireNonNull(newBuilder, "newBuilder");
        this.keyHandler = requireNonNull(keyHandler, "keyHandler");
        this.leafHasher = requireNonNull(leafHasher, "leafHasher");
        this.kind = requireNonNull(kind, "kind");
    }

    @Override
    public HashedTree<I, K> tree(Object value) {
        return switch (value) {
            case Map<?, ?> map -> {
                var hashedMap = transformMap(map, keyHandler, this::tree);
                var hash = hashMap(hashedMap);
                yield new Node<>(hash, hashedMap);
            }
            case Iterable<?> iterable -> {
                var hashedValues = transformList(iterable, this::tree);
                var hash = hashList(hashedValues);
                yield new Nodes<>(hash, hashedValues);
            }
            case null -> Null.instanceFor(kind);
            default -> {
                if (value.getClass().isArray()) {
                    var hashedValues = transformList(iterable(value), this::tree);
                    var hash = hashList(hashedValues);
                    yield new Nodes<>(hash, hashedValues);
                }
                yield new Leaf<>(hashLeaf(value), value);
            }
        };
    }

    private Hash<K> hashMap(Map<I, ? extends HashedTree<I, K>> tree) {
        var hb = newBuilder.get();
        HashBuilder<Hash<K>, K> hashHb = hb.map(Hash::toBytes);
        var keyHb = hb.map(keyHandler::bytes);
        hb.map(Bytes::intBytes).hash(tree.size());
        tree.forEach((key, value) -> {
            keyHb.hash(key);
            hashHb.hash(value.hash());
        });
        return hb.build();
    }

    private Hash<K> hashList(List<? extends HashedTree<I, K>> trees) {
        var hb = newBuilder.get();
        HashBuilder<Hash<K>, K> hashHb = hb.map(Hash::toBytes);
        hb.map(Bytes::intBytes).hash(trees.size());
        trees.stream()
            .map(HashedTree::hash)
            .forEach(hashHb::hash);
        return hashHb.build();
    }

    private Hash<K> hashLeaf(Object value) {
        return leafHasher.hash(value);
    }

    private static <MK, K extends HashKind<K>> Map<MK, HashedTree<MK, K>> transformMap(
        Map<?, ?> value,
        KeyHandler<MK> keyHandler,
        Function<Object, HashedTree<MK, K>> transform
    ) {
        return sizedMap(value.entrySet()
            .stream()
            .filter(empty().negate())
            .collect(Collectors.toMap(
                entry -> keyHandler.normalize(entry.getKey()),
                entry -> transform.apply(entry.getValue())
            )));
    }

    private static Predicate<Map.Entry<?, ?>> empty() {
        return entry -> isEmpty(entry.getValue());
    }

    private static boolean isEmpty(Object value) {
        return value != null && switch (value) {
            case Map<?, ?> map -> map.isEmpty();
            case Iterable<?> iterable -> !iterable.iterator().hasNext();
            case Object object -> isEmptyArray(object);
        };
    }
}
