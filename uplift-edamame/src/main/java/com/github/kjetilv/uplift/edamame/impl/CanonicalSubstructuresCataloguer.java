package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.CanonicalValue;
import com.github.kjetilv.uplift.edamame.Canonicalizer;
import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.kernel.util.Collectioons;
import com.github.kjetilv.uplift.kernel.util.Maps;

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
    implements Canonicalizer<K, H> {

    private final Map<Hash<H>, Map<K, Object>> maps = new ConcurrentHashMap<>();

    private final Map<Hash<H>, List<Object>> lists = new ConcurrentHashMap<>();

    private final Map<Hash<H>, Object> leaves = new ConcurrentHashMap<>();

    private final MapHasher<K, H> hasher;

    CanonicalSubstructuresCataloguer(MapHasher<K, H> hasher) {
        this.hasher = Objects.requireNonNull(hasher, "hasher");
    }

    @Override
    public CanonicalValue<H> canonical(Object value) {
        return canonical(hasher.hashedTree(value));
    }

    @Override
    public CanonicalValue<H> canonical(HashedTree<K, H> hashedTree) {
        return canonicalTree(hashedTree);
    }

    private CanonicalValue<H> canonicalTree(HashedTree<K, H> hashed) {
        return switch (hashed) {
            case HashedTree.Node<K, H>(Hash<H> hash, Map<K, HashedTree<K, H>> valueMap) -> {
                Map<K, CanonicalValue<H>> tree = recurse(valueMap, this::canonicalTree);
                yield collision(tree)
                    .map(supplant(hashed::hashed))
                    .orElseGet(() -> {
                        Map<K, Object> map = valueIn(tree);
                        return resolve(
                            hash,
                            maps.putIfAbsent(hash, map),
                            map,
                            t -> new CanonicalValue.Node<>(hash, t)
                        );
                    });
            }
            case HashedTree.Nodes<K, H>(Hash<H> hash, List<HashedTree<K, H>> values) -> {
                List<CanonicalValue<H>> trees = recurse(values, this::canonicalTree);
                yield collision(trees)
                    .map(supplant(hashed::hashed))
                    .orElseGet(() -> {
                        List<Object> list = valueIn(trees);
                        return resolve(
                            hash,
                            lists.putIfAbsent(hash, list),
                            list,
                            t -> new CanonicalValue.Nodes<>(hash, t)
                        );
                    });
            }
            case HashedTree.Leaf<?, H>(Hash<H> hash, Object value) -> resolve(
                hash,
                leaves.putIfAbsent(hash, value),
                value,
                t -> new CanonicalValue.Leaf<>(hash, t)
            );
            case HashedTree.Null<?, H>(Hash<H> hash) -> new CanonicalValue.Null<>(hash);
        };
    }

    private Map<K, CanonicalValue<H>> recurse(
        Map<K, HashedTree<K, H>> valueMap,
        Function<HashedTree<K, H>, CanonicalValue<H>> canonicalTree
    ) {
        return Maps.transformValues(valueMap, canonicalTree);
    }

    private List<CanonicalValue<H>> recurse(
        List<HashedTree<K, H>> values,
        Function<HashedTree<K, H>, CanonicalValue<H>> canonicalTree
    ) {
        return Collectioons.transform(values, canonicalTree);
    }

    private <T> CanonicalValue<H> resolve(Hash<H> hash, T existing, T value, Function<T, CanonicalValue<H>> wrap) {
        return existing == null ? wrap.apply(value)
            : existing.equals(value) ? wrap.apply(existing)
                : new CanonicalValue.Collision<>(hash, value);
    }

    private Function<CanonicalValue.Collision<H>, CanonicalValue<H>> supplant(Supplier<Object> value) {
        return collision -> new CanonicalValue.Collision<>(collision.hash(), value.get());
    }

    private Optional<CanonicalValue.Collision<H>> collision(Map<K, CanonicalValue<H>> tree) {
        return collision(tree.values());
    }

    @SuppressWarnings("RedundantTypeArguments") // Seems not
    private Optional<CanonicalValue.Collision<H>> collision(Collection<CanonicalValue<H>> values) {
        return values.stream()
            .filter(CanonicalValue.Collision.class::isInstance)
            .findFirst()
            .<CanonicalValue.Collision<H>>map(CanonicalValue.Collision.class::cast);
    }

    private static <H extends HashKind<H>> List<Object> valueIn(List<CanonicalValue<H>> canonicalValues) {
        return transform(canonicalValues, CanonicalValue::value);
    }

    private static <K, H extends HashKind<H>> Map<K, Object> valueIn(Map<K, CanonicalValue<H>> tree) {
        return transformValues(tree, CanonicalValue::value);
    }
}
