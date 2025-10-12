package com.github.kjetilv.uplift.edamame.impl;

import module java.base;
import module uplift.edamame;
import module uplift.hash;
import module uplift.util;

import static com.github.kjetilv.uplift.edamame.HashedTree.*;
import static com.github.kjetilv.uplift.util.Collectioons.*;
import static com.github.kjetilv.uplift.util.Maps.sizedMap;
import static java.util.Objects.requireNonNull;

/// Normalizes input trees and builds [hashed trees][HashedTree]. Stateless and thread-safe.
///
/// @param <K> Identifier type
record RecursiveTreeHasher<K, H extends HashKind<H>>(
    Supplier<HashBuilder<Bytes, H>> newBuilder,
    KeyHandler<K> keyHandler,
    LeafHasher<H> leafHasher,
    H kind
) implements TreeHasher<K, H> {

    /// @param newBuilder Hash builder, not null
    /// @param keyHandler Key handler, not null
    /// @param leafHasher Hasher, not null
    /// @see MapsMemoizers#create(KeyHandler, HashKind)
    RecursiveTreeHasher(
        Supplier<HashBuilder<Bytes, H>> newBuilder,
        KeyHandler<K> keyHandler,
        LeafHasher<H> leafHasher,
        H kind
    ) {
        this.newBuilder = requireNonNull(newBuilder, "newBuilder");
        this.keyHandler = requireNonNull(keyHandler, "keyHandler");
        this.leafHasher = requireNonNull(leafHasher, "leafHasher");
        this.kind = requireNonNull(kind, "kind");
    }

    @Override
    public HashedTree<K, H> hash(Object value) {
        return switch (value) {
            case Map<?, ?> map -> {
                var hashedMap =
                    transformMap(map, keyHandler, this::hash);
                yield new Node<>(
                    mapHash(hashedMap),
                    hashedMap
                );
            }
            case Iterable<?> iterable -> {
                var hashedValues =
                    transform(iterable, this::hash);
                yield new Nodes<>(
                    listHash(hashedValues),
                    hashedValues
                );
            }
            case null -> Null.instanceFor(kind);
            default -> {
                if (value.getClass().isArray()) {
                    var hashedValues =
                        transform(iterable(value), this::hash);
                    yield new Nodes<>(
                        listHash(hashedValues),
                        hashedValues
                    );
                } else {
                    yield new Leaf<>(leafHash(value), value);
                }
            }
        };
    }

    private static <K, H extends HashKind<H>> Map<K, HashedTree<K, H>> transformMap(
        Map<?, ?> value,
        KeyHandler<K> keyHandler,
        Function<Object, HashedTree<K, H>> transform
    ) {
        return sizedMap(value.entrySet()
            .stream()
            .filter(empty().negate())
            .collect(Collectors.toMap(
                entry -> keyHandler.normalize(entry.getKey()),
                entry -> transform.apply(entry.getValue())
            )));
    }

    private Hash<H> mapHash(Map<K, ? extends HashedTree<K, H>> tree) {
        var hb = newBuilder.get();
        HashBuilder<Hash<H>, H> hashHb = hb.map(Hash::toBytes);
        var keyHb = hb.map(keyHandler::toBytes);
        hb.<Integer>map(Hashes::intToBytes).hash(tree.size());
        tree.forEach((key, value) -> {
            keyHb.hash(key);
            hashHb.hash(value.hash());
        });
        return hb.build();
    }

    private Hash<H> listHash(List<? extends HashedTree<K, H>> trees) {
        var hb = newBuilder.get();
        HashBuilder<Hash<H>, H> hashHb = hb.map(Hash::toBytes);
        hb.<Integer>map(Hashes::intToBytes).hash(trees.size());
        trees.stream()
            .map(HashedTree::hash)
            .forEach(hashHb::hash);
        return hashHb.build();
    }

    private Hash<H> leafHash(Object value) {
        return leafHasher.hash(value);
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
