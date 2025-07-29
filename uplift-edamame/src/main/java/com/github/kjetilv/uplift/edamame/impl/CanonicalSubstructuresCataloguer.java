package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.CanonicalValue;
import com.github.kjetilv.uplift.edamame.CanonicalValue.Collision;
import com.github.kjetilv.uplift.edamame.Canonicalizer;
import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.HashedTree.Node;
import com.github.kjetilv.uplift.edamame.HashedTree.Nodes;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import static com.github.kjetilv.uplift.kernel.util.Collectioons.transform;
import static com.github.kjetilv.uplift.kernel.util.Maps.transformValues;

/**
 * Canonicalizes {@link HashedTree hashed trees}, progressively storing and resolving shared substructures
 * as they appear.
 * <p>
 * This class ought to be thread-safe, as it only appends to {@link ConcurrentMap concurrent maps}.
 *
 * @param <K>
 */
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
                yield collision(node.values())
                    .orElseGet(() ->
                        canonicalize(
                            maps,
                            hash,
                            transformValues(node, toValue()),
                            t ->
                                new CanonicalValue.Node<>(hash, t)
                        ));
            }
            case Nodes<K, H>(Hash<H> hash, List<HashedTree<K, H>> values) -> {
                List<CanonicalValue<H>> nodes = transform(values, toCanonical());
                yield collision(nodes)
                    .orElseGet(() ->
                        canonicalize(
                            lists,
                            hash,
                            transform(nodes, toValue()),
                            t ->
                                new CanonicalValue.Nodes<>(hash, t)
                        ));
            }
            case HashedTree.Leaf<?, H>(Hash<H> hash, Object value) -> canonicalize(
                leaves,
                hash,
                value,
                t -> new CanonicalValue.Leaf<>(hash, t)
            );
            case HashedTree.Null<?, H>(Hash<H> hash) -> new CanonicalValue.Null<>(hash);
        };
    }

    private Function<HashedTree<K, H>, CanonicalValue<H>> toCanonical() {
        return this::canonical;
    }

    private Optional<CanonicalValue<H>> collision(Collection<CanonicalValue<H>> values) {
        return values.stream()
            .filter(Collision.class::isInstance)
            .findFirst();
    }

    private <T> CanonicalValue<H> canonicalize(
        Map<Hash<H>, T> map,
        Hash<H> hash,
        T value,
        Function<T, CanonicalValue<H>> wrap
    ) {
        T existing = map.putIfAbsent(hash, value);
        return existing == null || collisionsNeverHappen ? wrap.apply(value)
            : existing.equals(value) ? wrap.apply(existing)
                : new Collision<>(hash, value);
    }

    private static <H extends HashKind<H>> Function<CanonicalValue<H>, Object> toValue() {
        return CanonicalValue::value;
    }
}
