package com.github.kjetilv.uplift.edamame.impl;

import module java.base;
import module uplift.edamame;
import module uplift.hash;
import com.github.kjetilv.uplift.edamame.CanonicalValue.Collision;
import com.github.kjetilv.uplift.edamame.HashedTree.Node;
import com.github.kjetilv.uplift.edamame.HashedTree.Nodes;

import static com.github.kjetilv.uplift.util.Collectioons.transform;
import static com.github.kjetilv.uplift.util.Maps.transformValues;

/// Canonicalizes [hashed trees][HashedTree], progressively storing and resolving shared substructures
/// as they appear.
///
/// This class ought to be thread-safe, as it only appends to [concurrent maps][ConcurrentMap].
///
/// @param <K>
final class CanonicalSubstructuresCataloguer<K, H extends HashKind<H>>
    implements Canonicalizer<K, H> {

    static <K, H extends HashKind<H>> Canonicalizer<K, H> create() {
        return new CanonicalSubstructuresCataloguer<K, H>();
    }

    private final Map<Hash<H>, Map<K, Object>> maps = new ConcurrentHashMap<>();

    private final Map<Hash<H>, List<Object>> lists = new ConcurrentHashMap<>();

    private final Map<Hash<H>, Object> leaves = new ConcurrentHashMap<>();

    private final boolean collisionsNeverHappen;

    private CanonicalSubstructuresCataloguer() {
        this(false);
    }

    CanonicalSubstructuresCataloguer(boolean collisionsNeverHappen) {
        this.collisionsNeverHappen = collisionsNeverHappen;
    }

    @Override
    public CanonicalValue<H> canonical(HashedTree<K, H> hashedTree) {
        return switch (hashedTree) {
            case Node<K, H>(var hash, var valueMap) -> {
                var node = transformValues(valueMap, this::canonical);
                var collision = collision(node.values());
                yield collision == null ? canonicalize(
                    transformValues(node, CanonicalValue::value),
                    hash,
                    maps,
                    t ->
                        new CanonicalValue.Node<>(hash, t)
                ) : collision;
            }
            case Nodes<K, H>(var hash, var values) -> {
                var nodes = transform(values, this::canonical);
                var collision = collision(nodes);
                yield collision == null ? canonicalize(
                    transform(nodes, CanonicalValue::value),
                    hash,
                    lists,
                    t ->
                        new CanonicalValue.Nodes<>(hash, t)
                ) : collision;
            }
            case HashedTree.Leaf<?, H>(var hash, var value) -> canonicalize(
                value,
                hash,
                leaves,
                t -> new CanonicalValue.Leaf<>(hash, t)
            );
            case HashedTree.Null<?, H>(var hash) -> new CanonicalValue.Null<>(hash);
        };
    }

    private Collision<H> collision(Iterable<CanonicalValue<H>> values) {
        for (var value : values) {
            if (value instanceof Collision<H> collision) {
                return collision;
            }
        }
        return null;
    }

    private <T> CanonicalValue<H> canonicalize(
        T value,
        Hash<H> hash,
        Map<Hash<H>, T> map,
        Function<T, CanonicalValue<H>> wrap
    ) {
        var existing = map.putIfAbsent(hash, value);
        return existing == null || collisionsNeverHappen ? wrap.apply(value)
            : existing.equals(value) ? wrap.apply(existing)
                : new Collision<>(hash, value);
    }
}
