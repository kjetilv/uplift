package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.edamame.MapsMemoizers;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.hash.Hashes;
import com.github.kjetilv.uplift.util.Bytes;
import com.github.kjetilv.uplift.util.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.kjetilv.uplift.edamame.HashedTree.*;
import static com.github.kjetilv.uplift.util.Collectioons.*;
import static java.util.Objects.requireNonNull;

/// Normalizes input trees and builds [hashed trees][HashedTree]. Stateless and thread-safe.
///
/// @param <K> Identifier type
record RecursiveTreeHasher<K, H extends HashKind<H>>(
    Supplier<HashBuilder<Bytes, H>> newBuilder,
    KeyHandler<K> keyHandler,
    LeafHasher<H> leafHasher,
    H kind
) implements MapHasher<K, H> {

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
    public HashedTree<K, H> hashedTree(Object value) {
        return switch (value) {
            case Map<?, ?> map -> {
                Map<K, HashedTree<K, H>> hashedMap = transformMap(map, keyHandler, this::hashedTree);
                yield new Node<>(mapHash(hashedMap), hashedMap);
            }
            case Iterable<?> iterable -> {
                List<HashedTree<K, H>> hashedValues = transform(iterable, this::hashedTree);
                yield new Nodes<>(listHash(hashedValues), hashedValues);
            }
            case null -> Null.instanceFor(kind);
            default -> {
                if (value.getClass().isArray()) {
                    List<HashedTree<K, H>> hashedValues = transform(iterable(value), this::hashedTree);
                    yield new Nodes<>(listHash(hashedValues), hashedValues);
                } else {
                    yield new Leaf<>(leafHash(value), value);
                }
            }
        };
    }

    private Hash<H> mapHash(Map<K, ? extends HashedTree<K, H>> tree) {
        HashBuilder<Bytes, H> hb = newBuilder.get();
        HashBuilder<Hash<H>, H> hashHb = hb.map((t) -> Bytes.from(t.bytes()));
        HashBuilder<K, H> keyHb = hb.map(key -> Bytes.from(keyHandler.bytes(key)));
        hb.<Integer>map(i -> Bytes.from(Hashes.bytes(i))).hash(tree.size());
        tree.forEach((key, value) -> {
            keyHb.hash(key);
            hashHb.hash(value.hash());
        });
        return hb.build();
    }

    private Hash<H> listHash(List<? extends HashedTree<K, H>> trees) {
        HashBuilder<Bytes, H> hb = newBuilder.get();
        HashBuilder<Hash<H>, H> hashHb = hb.map(hash -> Bytes.from(hash.bytes()));
        hb.<Integer>map(i -> Bytes.from(Hashes.bytes(i))).hash(trees.size());
        trees.stream()
            .map(HashedTree::hash)
            .forEach(hashHb::hash);
        return hashHb.build();
    }

    private Hash<H> leafHash(Object value) {
        return leafHasher.hash(value);
    }

    private static <K, H extends HashKind<H>> Map<K, HashedTree<K, H>> transformMap(
        Map<?, ?> value,
        KeyHandler<K> keyHandler,
        Function<Object, HashedTree<K, H>> transform
    ) {
        return Collections.unmodifiableMap(value.entrySet()
            .stream()
            .filter(empty().negate())
            .collect(Collectors.toMap(
                entry -> keyHandler.normalize(entry.getKey()),
                entry -> transform.apply(entry.getValue()),
                Maps.noMerge(),
                Maps.sizedMap(value.size())
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
