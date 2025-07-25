package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.*;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Works by hashing nodes and leaves and storing them under their hashes. When structures and/or values
 * re-occur, they are replaced by the already registered, canonical instances.
 * <p>
 * MD5 (128-bit) hashes are used. If an incoming value provokes a hash collision, it will be stored as-is and
 * separately from the canonical trees.  This should be rare.
 * <p>
 * Use {@link MapsMemoizers#create(HashKind)} and siblings to create instances of this class.
 *
 * @param <I> Identifier type.  An identifier identifies exactly one of the cached maps
 * @param <K> Key type for the maps. All maps (and their submaps) will be stored with keys of this type
 */
@SuppressWarnings("unchecked")
class MapsMemoizerImpl<I, K, H extends HashKind<H>>
    implements MapsMemoizer<I, K>, MemoizedMaps<I, K> {

    private final Map<I, Hash<H>> memoizedHashes = new HashMap<>();

    private final Map<Hash<H>, Map<K, Object>> canonicalObjects = new HashMap<>();

    private final Map<I, Map<K, Object>> overflowObjects = new HashMap<>();

    private final AtomicBoolean complete = new AtomicBoolean();

    private final ReadWriteLock canonicalLock = new ReentrantReadWriteLock();

    private final ReadWriteLock overflowLock = new ReentrantReadWriteLock();

    private final MapHasher<K, H> mapHasher;

    private Canonicalizer<K, H> canonicalValues;

    /**
     * @param canonicalValues Not null
     * @see MapsMemoizers#create(KeyHandler, HashKind)
     */
    MapsMemoizerImpl(MapHasher<K, H> mapHasher, Canonicalizer<K, H> canonicalValues) {
        this.mapHasher = requireNonNull(mapHasher, "mapHasher");
        this.canonicalValues = requireNonNull(canonicalValues, "canonicalValues");
    }

    @Override
    public int size() {
        return memoizedHashes.size() + overflowObjects.size();
    }

    @Override
    public Map<K, ?> get(I identifier) {
        requireNonNull(identifier, "identifier");
        return withReadLock(
            canonicalLock, () -> {
                Hash<H> hash = memoizedHashes.get(requireNonNull(identifier, "identifier"));
                return hash != null ? canonicalObjects.get(hash)
                    : !overflowObjects.isEmpty() ? overflowObjects.get(identifier)
                        : null;
            }
        );
    }

    @Override
    public void put(I identifier, Map<?, ?> value) {
        put(identifier, value, true);
    }

    @Override
    public boolean putIfAbsent(I identifier, Map<?, ?> value) {
        return put(identifier, value, false);
    }

    @Override
    public MemoizedMaps<I, K> complete() {
        return withWriteLock(
            canonicalLock, () -> {
                if (complete.compareAndSet(false, true)) {
                    // Shed working data
                    this.canonicalValues = null;
                }
                return this;
            }
        );
    }

    @SuppressWarnings({"unused"})
    private boolean put(I identifier, Map<?, ?> value, boolean requireAbsent) {
        requireNonNull(identifier, "identifier");
        requireNonNull(value, "value");
        HashedTree<K, H> hashedTree = mapHasher.hashedTree(value);
        CanonicalValue<H> canonical = canonicalValues.canonical(hashedTree);
        return switch (canonical) {
            case CanonicalValue.Node<?, H> valueNode -> withWriteLock(
                canonicalLock, () ->
                    putCanonical(
                        identifier,
                        valueNode,
                        requireAbsent
                    )
            );
            case CanonicalValue.Collision<H> collisionNode -> withWriteLock(
                overflowLock, () ->
                    putOverflow(identifier, (Map<K, Object>) value)
            );
            case CanonicalValue<H> other -> throw new IllegalStateException("Unexpected canonical value: " + other);
        };
    }

    private boolean putCanonical(I identifier, CanonicalValue.Node<?, H> valueNode, boolean requireAbsent) {
        if (!complete.get()) {
            Hash<H> existingHash = memoizedHashes.putIfAbsent(identifier, valueNode.hash());
            if (existingHash == null) {
                Map<K, Object> value = (Map<K, Object>) valueNode.value();
                Map<K, Object> existingValue = canonicalObjects.putIfAbsent(valueNode.hash(), value);
                if (existingValue == null || existingValue.equals(value)) {
                    return true;
                }
                throw new IllegalStateException("Illegal state: " + existingValue + " != " + value);
            }
            if (requireAbsent) {
                throw new IllegalArgumentException("Identifier " + identifier + " was: " + get(identifier));
            }
            return false;
        }
        throw new IllegalStateException(this + " is complete, cannot put " + identifier);
    }

    private boolean putOverflow(I identifier, Map<K, Object> value) {
        overflowObjects.put(
            identifier,
            value
        );
        return true;
    }

    private <T> T withReadLock(ReadWriteLock lock, Supplier<T> action) {
        return withLock(lock.readLock(), action);
    }

    private <T> T withWriteLock(ReadWriteLock lock, Supplier<T> action) {
        return withLock(lock.writeLock(), action);
    }

    private String doDescribe() {
        int count = memoizedHashes.size();
        int overflowsCount = overflowObjects.size();
        return (count + overflowsCount) +
               " items" +
               (overflowsCount == 0 ? ", " : " (" + overflowsCount + " collisions), ") +
               (complete.get() ? "completed" : "working maps:" + canonicalObjects.size());
    }

    private static <T> T withLock(Lock lock, Supplier<T> action) {
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + withReadLock(canonicalLock, this::doDescribe) + "]";
    }
}
