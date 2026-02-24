package com.github.kjetilv.uplift.hash;

import module java.base;
import com.github.kjetilv.uplift.util.Bytes;

/// Maintains a simple pool of non-trivially-created instances of [MessageDigest], in the form of a queue.
/// On demand, query the queue for a digest, or create a new one.  On release, offer the digest to the queue.
///
/// @param <K>
final class MessageByteDigest<K extends HashKind<K>> implements ByteDigest<K> {

    static <K extends HashKind<K>> MessageByteDigest<K> get(K kind) {
        return new MessageByteDigest<>(kind);
    }

    private final K kind;

    private final Lock digestLock = new ReentrantLock();

    private MessageDigest messageDigest;

    MessageByteDigest(K kind) {
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    @Override
    public K kind() {
        return kind;
    }

    @Override
    public void digest(Bytes bs) {
        currentDigest().update(bs.bytes(), bs.offset(), bs.length());
    }

    @Override
    public void digest(ByteBuffer bytes) {
        currentDigest().update(bytes);
    }

    /// Drain current digest, unset it and offer it to the queue.
    ///
    /// @return Hash of current digest
    @Override
    public Hash<K> get() {
        digestLock.lock();
        try {
            if (messageDigest == null) {
                return kind.blank();
            }
            try {
                return Hash.from(messageDigest.digest());
            } finally {
                var unset = messageDigest;
                messageDigest = null;
                queue(kind).offer(unset);
            }
        } finally {
            digestLock.unlock();
        }
    }

    /// Get the current digest.  If none is set, dequeue one. If queue was empty, create a new one.
    ///
    /// @return Current digest
    private MessageDigest currentDigest() {
        digestLock.lock();
        try {
            if (messageDigest == null) {
                var pooled = queue(kind).pollFirst();
                messageDigest = pooled == null ? createDigest(kind) : pooled;
            }
            return messageDigest;
        } finally {
            digestLock.unlock();
        }
    }

    private static final Map<HashKind<?>, Deque<MessageDigest>> QUEUE = new ConcurrentHashMap<>();

    private static final int DIGEST_POOL_SIZE = 20;

    private static <K extends HashKind<K>> MessageDigest createDigest(K kind) {
        try {
            return MessageDigest.getInstance(kind.algorithm());
        } catch (Exception e) {
            throw new IllegalStateException("Expected " + kind.algorithm() + " implementation", e);
        }
    }

    private static Deque<MessageDigest> queue(HashKind<?> kind) {
        return QUEUE.computeIfAbsent(
            kind, _ ->
                new LinkedBlockingDeque<>(DIGEST_POOL_SIZE)
        );
    }

    @Override
    public String toString() {
        return getClass().getName() + "[" + kind + "]";
    }
}
