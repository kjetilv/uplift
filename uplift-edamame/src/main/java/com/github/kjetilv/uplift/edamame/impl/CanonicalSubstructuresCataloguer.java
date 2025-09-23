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

    private final Map<Hash<H>, Map<K, Object>> maps = new ConcurrentHashMap<>();

    private final Map<Hash<H>, List<Object>> lists = new ConcurrentHashMap<>();

    private final Map<Hash<H>, Object> leaves = new ConcurrentHashMap<>();

    private final boolean collisionsNeverHappen;

    CanonicalSubstructuresCataloguer() {
        this(false);
    }

    CanonicalSubstructuresCataloguer(boolean collisionsNeverHappen) {
        this.collisionsNeverHappen = collisionsNeverHappen;
    }

    @Override
    public CanonicalValue<H> canonical(HashedTree<K, H> hashedTree) {
        return switch (hashedTree) {
            case Node<K, H>(Hash<H> hash, Map<K, HashedTree<K, H>> valueMap) -> {
                Map<K, CanonicalValue<H>> node = transformValues(valueMap, toCanonical());
                Collision<H> collision = collision(node.values());
                yield collision == null ? canonicalize(
                    transformValues(node, toValue()),
                    hash,
                    maps,
                    t ->
                        new CanonicalValue.Node<>(hash, t)
                ) : collision;
            }
            case Nodes<K, H>(Hash<H> hash, List<HashedTree<K, H>> values) -> {
                List<CanonicalValue<H>> nodes = transform(values, toCanonical());
                Collision<H> collision = collision(nodes);
                yield collision == null ? canonicalize(
                    transform(nodes, toValue()),
                    hash,
                    lists,
                    t ->
                        new CanonicalValue.Nodes<>(hash, t)
                ) : collision;
            }
            case HashedTree.Leaf<?, H>(Hash<H> hash, Object value) -> canonicalize(
                value,
                hash,
                leaves,
                t -> new CanonicalValue.Leaf<>(hash, t)
            );
            case HashedTree.Null<?, H>(
                Hash<H> hash
            ) -> new CanonicalValue.Null<>(hash);
        };
    }

    private Collision<H> collision(Iterable<CanonicalValue<H>> values) {
        for (CanonicalValue<H> value : values) {
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
        T existing = map.putIfAbsent(hash, value);
        return existing == null || collisionsNeverHappen ? wrap.apply(value)
            : existing.equals(value) ? wrap.apply(existing)
                : new Collision<>(hash, value);
    }

    private Function<HashedTree<K, H>, CanonicalValue<H>> toCanonical() {
        return this::canonical;
    }

    private static <H extends HashKind<H>> Function<CanonicalValue<H>, Object> toValue() {
        return CanonicalValue::value;
    }
}
