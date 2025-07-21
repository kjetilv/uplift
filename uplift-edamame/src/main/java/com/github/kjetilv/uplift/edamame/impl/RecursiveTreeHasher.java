package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.MapsMemoizers;
import com.github.kjetilv.uplift.hash.*;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.Hashes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.kjetilv.uplift.edamame.impl.CollectionUtils.*;
import static com.github.kjetilv.uplift.edamame.impl.HashedTree.*;
import static java.util.Objects.requireNonNull;

/**
 * Normalizes input trees and builds {@link HashedTree hashed trees}. Stateless and thread-safe.
 * <p>
 *
 * @param <S> Identifier type
 */
record RecursiveTreeHasher<S, H extends HashKind<H>>(
    Supplier<HashBuilder<Bytes, H>> newBuilder,
    KeyHandler<S> keyHandler,
    LeafHasher<H> leafHasher,
    H kind
) {

    /**
     * @param newBuilder Hash builder, not null
     * @param keyHandler Key handler, not null
     * @param leafHasher Hasher, not null
     * @see MapsMemoizers#create(KeyHandler)
     */
    RecursiveTreeHasher(
        Supplier<HashBuilder<Bytes, H>> newBuilder,
        KeyHandler<S> keyHandler,
        LeafHasher<H> leafHasher,
        H kind
    ) {
        this.newBuilder = requireNonNull(newBuilder, "newBuilder");
        this.keyHandler = requireNonNull(keyHandler, "keyHandler");
        this.leafHasher = requireNonNull(leafHasher, "leafHasher");
        this.kind = requireNonNull(kind, "kind");
    }

    @SuppressWarnings("unchecked")
    HashedTree<?, H> hashedTree(Object value) {
        return value == null
            ? new Null<>(kind.blank())
            : switch (value) {
                case Map<?, ?> map -> nodeForMap((Map<S, Object>) map);
                case Iterable<?> iterable -> nodesForIterable(iterable);
                default -> value.getClass().isArray()
                    ? nodesForIterable(iterable(value))
                    : leafFor(value);
            };
    }

    private Nodes<H> nodesForIterable(Iterable<?> iterable) {
        List<? extends HashedTree<?, H>> hashedValues = transform(iterable, this::hashedTree);
        return new Nodes<H>(listHash(hashedValues), hashedValues);
    }

    private Node<S, H> nodeForMap(Map<S, Object> map) {
        Map<S, HashedTree<?, H>> hashedMap = normalized(map);
        return new Node<S, H>(mapHash(hashedMap), hashedMap);
    }

    private Leaf<H> leafFor(Object value) {
        return new Leaf<>(leafHash(value), value);
    }

    private Hash<H> listHash(List<? extends HashedTree<?, H>> trees) {
        HashBuilder<Bytes, H> hb = newBuilder.get();
        HashBuilder<Hash<H>, H> hashHb = hb.map(hash -> Bytes.from(hash.bytes()));
        hb.<Integer>map(i -> Bytes.from(Hashes.bytes(i))).hash(trees.size());
        trees.stream()
            .map(HashedTree::hash)
            .forEach(hashHb);
        return hashHb.get();
    }

    private Hash<H> mapHash(Map<S, ? extends HashedTree<?, H>> tree) {
        HashBuilder<Bytes, H> hb = newBuilder.get();
        HashBuilder<Hash<H>, H> hashHb =
            hb.map((t) -> Bytes.from(t.bytes()));
        HashBuilder<S, H> keyHb = hb.map(key -> Bytes.from(keyHandler.bytes(key)));
        hb.<Integer>map(i -> Bytes.from(Hashes.bytes(i))).hash(tree.size());
        tree.forEach((key, value) -> {
            keyHb.hash(key);
            hashHb.apply(value.hash());
        });
        return hb.get();
    }

    private Hash<H> leafHash(Object value) {
        return leafHasher.hash(value);
    }

    private Map<S, HashedTree<?, H>> normalized(Map<?, ?> value) {
        return Collections.unmodifiableMap(value.entrySet()
            .stream()
            .filter(hasData())
            .collect(Collectors.toMap(
                entry -> keyHandler.normalize(entry.getKey()),
                entry -> hashedTree(entry.getValue()),
                noMerge(),
                sizedMap(value.size())
            )));
    }

    private static Predicate<Map.Entry<?, ?>> hasData() {
        return entry -> !isEmpty(entry.getValue());
    }

    private static boolean isEmpty(Object value) {
        return value != null && switch (value) {
            case Map<?, ?> map -> map.isEmpty();
            case Iterable<?> iterable -> !iterable.iterator().hasNext();
            case Object object -> object.getClass().isArray() && isEmpty(iterable(object));
        };
    }
}
