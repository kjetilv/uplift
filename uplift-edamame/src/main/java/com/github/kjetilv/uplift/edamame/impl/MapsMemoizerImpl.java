package com.github.kjetilv.uplift.edamame.impl;

import module java.base;
import com.github.kjetilv.uplift.edamame.*;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import static java.util.Objects.requireNonNull;

/// Works by hashing nodes and leaves and storing them under their hashes. When structures and/or values
/// re-occur, they are replaced by the already registered, canonical instances.
///
/// If an incoming value provokes a hash collision, it will be stored as-is and separately from the canonical
/// trees.  This should be rare, especially with [SHA3-256][com.github.kjetilv.uplift.hash.HashKind.K256].
///
/// Use [MapsMemoizers#create(HashKind)] and siblings to create instances of this class.
///
/// @param <I> Identifier type.  An identifier identifies exactly one of the cached maps
/// @param <K> Key type for the maps. All maps (and their submaps) will be stored with keys of this type
/// @param <H> Hash kind
@SuppressWarnings("unchecked")
class MapsMemoizerImpl<I, K, H extends HashKind<H>>
    implements MapsMemoizer<I, K> {

    private final Map<I, Hash<H>> memoizedHashes = new HashMap<>();

    private final Map<Hash<H>, Map<K, Object>> canonicalObjects = new HashMap<>();

    private final Map<I, Map<K, Object>> overflowObjects = new HashMap<>();

    private final AtomicBoolean complete = new AtomicBoolean();

    private final ReadWriteLock canonicalLock = new ReentrantReadWriteLock();

    private final ReadWriteLock overflowLock = new ReentrantReadWriteLock();

    private final TreeHasher<K, H> treeHasher;

    private Canonicalizer<K, H> canonicalValues;

    /// @param canonicalValues Not null
    /// @see MapsMemoizers#create(com.github.kjetilv.uplift.edamame.KeyHandler, HashKind)
    MapsMemoizerImpl(TreeHasher<K, H> treeHasher, Canonicalizer<K, H> canonicalValues) {
        this.treeHasher = requireNonNull(treeHasher, "mapHasher");
        this.canonicalValues = requireNonNull(canonicalValues, "canonicalValues");
    }

    @Override
    public int size() {
        return canonicalRead(memoizedHashes::size) + overflowReadLocked(overflowObjects::size);
    }

    @Override
    public Map<K, ?> get(I identifier) {
        requireNonNull(identifier, "identifier");
        return canonicalRead(() -> {
            var hash = memoizedHashes.get(requireNonNull(identifier, "identifier"));
            return hash != null ? canonicalObjects.get(hash)
                : overflowReadLocked(() -> !overflowObjects.isEmpty()
                    ? overflowObjects.get(identifier)
                    : null);
        });
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
        return canonicalWrite(() -> {
            if (complete.compareAndSet(false, true)) {
                // Shed working data
                this.canonicalValues = null;
            }
            return this;
        });
    }

    @SuppressWarnings({"unused"})
    private boolean put(I identifier, Map<?, ?> value, boolean requireAbsent) {
        requireNonNull(identifier, "identifier");
        requireNonNull(value, "value");
        var hashedTree = treeHasher.hash(value);
        var canonical = canonicalValues.canonical(hashedTree);
        return switch (canonical) {
            case CanonicalValue.Node<?, H> node -> canonicalWrite(() ->
                putCanonical(identifier, node, requireAbsent)
            );
            case CanonicalValue.Collision<H> _ -> overflowWrite(() ->
                putOverflow(identifier, (Map<K, Object>) value)
            );
            case CanonicalValue<H> other -> fail("Unexpected canonical value: " + other);
        };
    }

    private boolean putCanonical(I identifier, CanonicalValue.Node<?, H> valueNode, boolean requireAbsent) {
        if (complete.get()) {
            return fail(this + " is complete, cannot put " + identifier);
        }
        var existingHash = memoizedHashes.putIfAbsent(identifier, valueNode.hash());
        if (existingHash == null) {
            var value = (Map<K, Object>) valueNode.value();
            var existingValue = canonicalObjects.putIfAbsent(valueNode.hash(), value);
            if (existingValue == null || existingValue.equals(value)) {
                return true;
            }
            return fail("Illegal state: " + existingValue + " != " + value);
        }
        if (requireAbsent) {
            throw new IllegalArgumentException("Identifier " + identifier + " was: " + get(identifier));
        }
        return false;
    }

    private boolean putOverflow(I identifier, Map<K, Object> value) {
        return overflowObjects.putIfAbsent(identifier, value) == null;
    }

    private String doDescribe() {
        int overflowsCount = overflowReadLocked(overflowObjects::size);
        return canonicalRead(() -> {
            var count = memoizedHashes.size();
            var canonicalsSize = canonicalObjects.size();
            return (count + overflowsCount) +
                   " items" +
                   (overflowsCount == 0 ? ", " : " (" + overflowsCount + " collisions), ") +
                   (complete.get() ? "completed" : "working maps:" + canonicalsSize);
        });
    }

    private <T> T canonicalRead(Supplier<T> action) {
        return withLock(canonicalLock.readLock(), action);
    }

    private <T> T canonicalWrite(Supplier<T> action) {
        return withLock(canonicalLock.writeLock(), action);
    }

    private <T> T overflowReadLocked(Supplier<T> action) {
        return withLock(overflowLock.readLock(), action);
    }

    private <T> T overflowWrite(Supplier<T> action) {
        return withLock(overflowLock.writeLock(), action);
    }

    private static <T> T withLock(Lock lock, Supplier<T> action) {
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    private static <T> T fail(String other) {
        throw new IllegalStateException(other);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               overflowReadLocked(() ->
                   canonicalRead(this::doDescribe)) +
               "]";
    }
}
