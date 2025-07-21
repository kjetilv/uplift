package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.MapsMemoizer;
import com.github.kjetilv.uplift.edamame.MapsMemoizers;
import com.github.kjetilv.uplift.edamame.MemoizedMaps;
import com.github.kjetilv.uplift.hash.Bytes;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    implements MapsMemoizer<I, K>, MemoizedMaps<I, K>, KeyHandler<K> {

    private final Map<I, Hash<H>> memoizedHashes = new HashMap<>();

    private final Map<Hash<H>, Map<K, Object>> canonicalObjects = new HashMap<>();

    private final Map<I, Map<K, Object>> overflowObjects = new HashMap<>();

    private final AtomicBoolean complete = new AtomicBoolean();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private Map<Object, K> canonicalKeys = new ConcurrentHashMap<>();

    private Map<K, byte[]> canonicalBytes = new ConcurrentHashMap<>();

    private Canonicalizer<H> canonicalizer;

    private final KeyHandler<K> keyHandler;

    /**
     * @param newBuilder Hash builder, not null
     * @param keyHandler Key handler, not null
     * @param leafHasher Hasher, not null
     * @see MapsMemoizers#create(KeyHandler, HashKind)
     */
    MapsMemoizerImpl(
        Supplier<HashBuilder<Bytes, H>> newBuilder,
        KeyHandler<K> keyHandler,
        LeafHasher<H> leafHasher,
        H kind
    ) {
        requireNonNull(keyHandler, "key handler");
        this.keyHandler = keyHandler;
        MapHasher<H> khRecursiveTreeHasher = new RecursiveTreeHasher<>(
            requireNonNull(newBuilder, "newBuilder"),
            this,
            requireNonNull(leafHasher, "leafHasher"),
            kind
        );
        this.canonicalizer = new CanonicalSubstructuresCataloguer<>(
            khRecursiveTreeHasher);
    }

    @Override
    public void put(I identifier, Map<?, ?> value) {
        put(
            requireNonNull(identifier, "identifier"),
            requireNonNull(value, "value"),
            true
        );
    }

    @Override
    public boolean putIfAbsent(I identifier, Map<?, ?> value) {
        return put(
            requireNonNull(identifier, "identifier"),
            requireNonNull(value, "value"),
            false
        );
    }

    @Override
    public int size() {
        return memoizedHashes.size() + overflowObjects.size();
    }

    @Override
    public Map<K, ?> get(I identifier) {
        requireNonNull(identifier, "identifier");
        return withReadLock(() -> {
            Hash<H> hash = memoizedHashes.get(requireNonNull(identifier, "identifier"));
            return hash != null ? canonicalObjects.get(hash)
                : !overflowObjects.isEmpty() ? overflowObjects.get(identifier)
                    : null;
        });
    }

    @Override
    public MemoizedMaps<I, K> complete() {
        if (complete.compareAndSet(false, true)) {
            withWriteLock(() -> {
                // Shed working data
                this.canonicalizer = null;
                this.canonicalKeys = null;
                this.canonicalBytes = null;
                return this;
            });
        }
        return this;
    }

    @Override
    public K normalize(Object key) {
        return canonicalKeys.computeIfAbsent(key, keyHandler::normalize);
    }

    @Override
    public byte[] bytes(K key) {
        return canonicalBytes.computeIfAbsent(key, keyHandler::bytes);
    }

    @SuppressWarnings({"unused"})
    private boolean put(I identifier, Map<?, ?> value, boolean failOnConflict) {
        if (rejected(identifier, failOnConflict)) {
            return false;
        }
        CanonicalValue<H> canonical = canonicalizer.canonicalMap(value);
        return withWriteLock(() -> {
            switch (canonical) {
                case CanonicalValue.Node<?, H> valueNode -> {
                    memoizedHashes.put(identifier, valueNode.hash());
                    canonicalObjects.put(
                        valueNode.hash(),
                        (Map<K, Object>) valueNode.value()
                    );
                }
                case CanonicalValue.Collision<H> collisionNode -> overflowObjects.put(
                    identifier,
                    (Map<K, Object>) value
                );
                case CanonicalValue<H> other -> throw new IllegalStateException("Unexpected canonical value: " + other);
            }
            return true;
        });
    }

    private boolean rejected(I identifier, boolean failOnConflict) {
        if (complete.get()) {
            throw new IllegalStateException(this + " is complete, cannot put " + identifier);
        }
        if (memoizedHashes.containsKey(identifier)) {
            if (failOnConflict) {
                throw new IllegalArgumentException("Identifier " + identifier + " was:" + get(identifier));
            }
            return true;
        }
        return false;
    }

    private <T> T withReadLock(Supplier<T> action) {
        return withLock(lock.readLock(), action);
    }

    private <T> T withWriteLock(Supplier<T> action) {
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
        return getClass().getSimpleName() + "[" + withReadLock(this::doDescribe) + "]";
    }
}
