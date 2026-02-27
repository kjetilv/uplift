package com.github.kjetilv.uplift.edamame.impl;

import module java.base;
import com.github.kjetilv.uplift.edamame.CanonicalValue;
import com.github.kjetilv.uplift.edamame.CanonicalValue.Collision;
import com.github.kjetilv.uplift.edamame.Canonicalizer;
import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.HashedTree.Node;
import com.github.kjetilv.uplift.edamame.HashedTree.Nodes;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import static com.github.kjetilv.uplift.util.Collectioons.transformList;
import static com.github.kjetilv.uplift.util.Maps.transformMap;

/// Canonicalizes [hashed trees][HashedTree], progressively storing and resolving shared substructures
/// as they appear.
///
/// This class ought to be thread-safe, as it only appends to [concurrent maps][ConcurrentMap].
///
/// @param <MK>
final class CanonicalSubstructuresCataloguer<MK, K extends HashKind<K>>
    implements Canonicalizer<MK, K> {

    static <MK, K extends HashKind<K>> Canonicalizer<MK, K> create() {
        return new CanonicalSubstructuresCataloguer<>();
    }

    private final Map<Hash<K>, Map<MK, Object>> maps = new ConcurrentHashMap<>();

    private final Map<Hash<K>, List<Object>> lists = new ConcurrentHashMap<>();

    private final Map<Hash<K>, Object> leaves = new ConcurrentHashMap<>();

    private final boolean collisionsNeverHappen;

    CanonicalSubstructuresCataloguer(boolean collisionsNeverHappen) {
        this.collisionsNeverHappen = collisionsNeverHappen;
    }

    private CanonicalSubstructuresCataloguer() {
        this(false);
    }

    @Override
    public CanonicalValue<K> canonical(HashedTree<MK, K> hashedTree) {
        return switch (hashedTree) {
            case Node<MK, K>(var hash, var map) -> {
                var node = transformMap(map, this::canonical);
                var collision = collision(node.values());
                yield collision != null ? collision
                    : canonicalize(
                        transformMap(node, CanonicalValue::value),
                        hash,
                        maps,
                        t ->
                            new CanonicalValue.Node<>(hash, t)
                    );
            }
            case Nodes<MK, K>(var hash, var list) -> {
                var nodes = transformList(list, this::canonical);
                var collision = collision(nodes);
                yield collision != null ? collision
                    : canonicalize(
                        transformList(nodes, CanonicalValue::value),
                        hash,
                        lists,
                        t ->
                            new CanonicalValue.Nodes<>(hash, t)
                    );
            }
            case HashedTree.Leaf<?, K>(var hash, var value) -> canonicalize(
                value,
                hash,
                leaves,
                t -> new CanonicalValue.Leaf<>(hash, t)
            );
            case HashedTree.Null<?, K>(var kind) -> CanonicalValue.Null.instanceFor(kind);
        };
    }

    private Collision<K> collision(Iterable<CanonicalValue<K>> values) {
        for (var value : values) {
            if (value instanceof Collision<K> collision) {
                return collision;
            }
        }
        return null;
    }

    private <T> CanonicalValue<K> canonicalize(
        T value,
        Hash<K> hash,
        Map<Hash<K>, T> map,
        Function<T, CanonicalValue<K>> wrap
    ) {
        var existing = map.putIfAbsent(hash, value);
        return existing == null || collisionsNeverHappen ? wrap.apply(value)
            : existing.equals(value) ? wrap.apply(existing)
                : new Collision<>(hash, value);
    }
}
