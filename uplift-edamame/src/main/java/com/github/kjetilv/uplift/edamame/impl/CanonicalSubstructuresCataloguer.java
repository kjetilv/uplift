package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
final class CanonicalSubstructuresCataloguer<K, H extends HashKind<H>> {

    private final Map<Hash<H>, Map<K, Object>> maps = new ConcurrentHashMap<>();

    private final Map<Hash<H>, List<Object>> lists = new ConcurrentHashMap<>();

    private final Map<Hash<H>, Object> leaves = new ConcurrentHashMap<>();

    /**
     * Accepts a {@link HashedTree hashed tree} and returns the {@link CanonicalValue canonical value}.
     * Traverses the {@link HashedTree hashed tree} and re-builds it.  New substructures found in incoming
     * structures are recorded under their respective {@link HashedTree#hash() hashes}.  If the hash is
     * recorded already, that occurrence is retrieved and used to replace the incoming one.
     * <p>
     * This method tries to show the recursive flow with a minimum of fuss, which is why it calls out
     * to a lot of one-liners, which would otherwise add up to a lot of clutter.
     *
     * @param hashedTree Hashed tree
     * @return A {@link CanonicalValue value} which may be either a {@link CanonicalValue.Collision collision},
     * or a holder for the canonical value
     */
    @SuppressWarnings("unchecked")
    public CanonicalValue toCanonical(HashedTree<?, H> hashedTree) {
        return switch (hashedTree) {
            case HashedTree.Node<?, ?>(Hash<?> hash, Map<?, ? extends HashedTree<?, ?>> valueMap) -> {
                Map<K, CanonicalValue> canonicalTrees = recurseMap((Map<K, HashedTree<?, H>>) valueMap);
                yield collision(canonicalTrees).orElseGet(() -> {
                    Map<K, Object> map = mapValue(canonicalTrees);
                    return resolve(
                        cataloguedMap((Hash<H>) hash, map),
                        map,
                        CanonicalValue.Node::new
                    );
                });
            }
            case HashedTree.Nodes<?>(Hash<?> hash, List<? extends HashedTree<?, ?>> values) -> {
                List<CanonicalValue> canonicalValues = recurseList((List<? extends HashedTree<?, H>>) values);
                yield collision(canonicalValues).orElseGet(() -> {
                    List<Object> list = listValue(canonicalValues);
                    return resolve(
                        cataloguedList((Hash<H>) hash, list),
                        list,
                        CanonicalValue.Nodes::new
                    );
                });
            }
            case HashedTree.Leaf<?>(Hash<?> hash, Object value) -> resolve(
                cataloguedLeaf((Hash<H>) hash, value),
                value,
                CanonicalValue.Leaf::new
            );
            case HashedTree.Null<?> ignored -> CanonicalValue.NULL;
        };
    }

    private Map<K, CanonicalValue> recurseMap(Map<K, HashedTree<?, H>> hashedTrees) {
        return transformValues(hashedTrees, this::toCanonical);
    }

    private List<CanonicalValue> recurseList(List<? extends HashedTree<?, H>> values) {
        return transform(values, this::toCanonical);
    }

    private Map<K, Object> cataloguedMap(Hash<H> hash, Map<K, Object> computed) {
        return maps.putIfAbsent(hash, computed);
    }

    private List<Object> cataloguedList(Hash<H> hash, List<Object> computed) {
        return lists.putIfAbsent(hash, computed);
    }

    private Object cataloguedLeaf(Hash<H> hash, Object value) {
        return leaves.putIfAbsent(hash, value);
    }

    private static <T> CanonicalValue resolve(T existing, T value, Function<T, CanonicalValue> wrap) {
        return existing == null ? wrap.apply(value)
            : existing.equals(value) ? wrap.apply(existing)
                : CanonicalValue.COLLISION;
    }

    private static List<Object> listValue(List<CanonicalValue> canonicalValues) {
        return transform(canonicalValues, CanonicalValue::value);
    }

    private static <K> Map<K, Object> mapValue(Map<K, CanonicalValue> canonicalTrees) {
        return transformValues(canonicalTrees, CanonicalValue::value);
    }

    /**
     * Collision, of any
     *
     * @param map Map
     * @return Any collision
     */
    private static Optional<CanonicalValue> collision(Map<?, CanonicalValue> map) {
        return collision(map.values());
    }

    /**
     * Collision, of any
     *
     * @param values List
     * @return Any collision
     */
    private static Optional<CanonicalValue> collision(Collection<CanonicalValue> values) {
        return values.stream()
            .filter(CanonicalValue::collision)
            .findFirst();
    }
}
