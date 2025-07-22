package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;

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
    implements Canonicalizer<H> {

    private final Map<Hash<H>, Map<K, Object>> maps = new ConcurrentHashMap<>();

    private final Map<Hash<H>, List<Object>> lists = new ConcurrentHashMap<>();

    private final Map<Hash<H>, Object> leaves = new ConcurrentHashMap<>();

    private final MapHasher<H> hasher;

    CanonicalSubstructuresCataloguer(MapHasher<H> hasher) {
        this.hasher = Objects.requireNonNull(hasher, "hasher");
    }

    @Override
    public CanonicalValue<H> canonical(Object value) {
        HashedTree<H> hashed = hasher.hashedTree(value);
        return canonicalTree(hashed);
    }

    @SuppressWarnings("unchecked")
    private CanonicalValue<H> canonicalTree(HashedTree<H> hashed) {
        return switch (hashed) {
            case HashedTree.Node<?, H>(Hash<H> hash, Map<?, ? extends HashedTree<H>> valueMap) -> {
                Map<K, CanonicalValue<H>> tree = transformValues(
                    (Map<K, HashedTree<H>>) valueMap,
                    this::canonicalTree
                );
                yield collision(tree.values()).map(supplant(hashed::hashed))
                    .orElseGet(() -> {
                        Map<K, Object> map = transformValues(tree, CanonicalValue::value);
                        return resolve(
                            hash,
                            maps.putIfAbsent(hash, map),
                            map,
                            t -> new CanonicalValue.Node<>(hash, t)
                        );
                    });
            }
            case HashedTree.Nodes<H>(Hash<H> hash, List<? extends HashedTree<H>> values) -> {
                List<CanonicalValue<H>> canonicalValues = transform(
                    values,
                    this::canonicalTree
                );
                yield collision(canonicalValues).map(supplant(hashed::hashed))
                    .orElseGet(() -> {
                        List<Object> list = transform(canonicalValues, CanonicalValue::value);
                        return resolve(
                            hash,
                            lists.putIfAbsent(hash, list),
                            list,
                            t -> new CanonicalValue.Nodes<>(hash, t)
                        );
                    });
            }
            case HashedTree.Leaf<H>(Hash<H> hash, Object value) -> resolve(
                hash,
                leaves.putIfAbsent(hash, value),
                value,
                t -> new CanonicalValue.Leaf<>(hash, t)
            );
            case HashedTree.Null<H>(Hash<H> hash) -> new CanonicalValue.Null<>(hash);
        };
    }

    private <T> CanonicalValue<H> resolve(Hash<H> hash, T existing, T value, Function<T, CanonicalValue<H>> wrap) {
        return existing == null ? wrap.apply(value)
            : existing.equals(value) ? wrap.apply(existing)
                : new CanonicalValue.Collision<>(hash, value);
    }

    private Function<CanonicalValue.Collision<H>, CanonicalValue<H>> supplant(Supplier<Object> value) {
        return collision -> new CanonicalValue.Collision<>(collision.hash(), value.get());
    }

    @SuppressWarnings("RedundantTypeArguments") // Seems not
    private Optional<CanonicalValue.Collision<H>> collision(Collection<CanonicalValue<H>> values) {
        return values.stream()
            .filter(CanonicalValue.Collision.class::isInstance)
            .findFirst()
            .<CanonicalValue.Collision<H>>map(CanonicalValue.Collision.class::cast);
    }
}
