package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import static com.github.kjetilv.uplift.edamame.impl.CollectionUtils.transform;
import static com.github.kjetilv.uplift.edamame.impl.CollectionUtils.transformValues;

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

    /**
     * Accepts a nested, JSON-like {@link Map map} and returns its {@link CanonicalValue canonical value}:
     * All {@link Object#equals(Object) equals} values will be replaced by the same object reference.
     * <p>
     * Traverses the {@link HashedTree hashed tree} and re-builds it.  New substructures found in incoming
     * structures are recorded under their respective {@link HashedTree#hash() hashes}.  If the hash is
     * recorded already, that occurrence is retrieved and used to replace the incoming one.
     * <p>
     * This method tries to show the recursive flow with a minimum of fuss, which is why it calls out
     * to a lot of one-liners, which would otherwise add up to a lot of clutter.
     *
     * @param value A map
     * @return A {@link CanonicalValue value} which may be either a {@link CanonicalValue.Collision collision},
     * or a holder for the canonical value
     */
    @Override
    public CanonicalValue<H> canonicalMap(Map<?, ?> value) {
        return canonicalTree(hasher.hashedTree(value));
    }

    @SuppressWarnings("unchecked")
    private CanonicalValue<H> canonicalTree(HashedTree<H> hashed) {
        return switch (hashed) {
            case HashedTree.Node<?, H>(Hash<H> hash, Map<?, ? extends HashedTree<H>> valueMap) -> {
                Map<K, CanonicalValue<H>> tree = transformValues(
                    (Map<K, HashedTree<H>>) valueMap,
                    this::canonicalTree
                );
                yield collision(tree.values()).orElseGet(() -> {
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
                yield collision(canonicalValues).orElseGet(() -> {
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

    private Optional<CanonicalValue<H>> collision(Collection<CanonicalValue<H>> values) {
        return values.stream().filter(CanonicalValue.Collision.class::isInstance).findFirst();
    }
}
