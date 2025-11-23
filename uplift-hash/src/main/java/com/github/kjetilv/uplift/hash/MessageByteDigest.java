package com.github.kjetilv.uplift.hash;

import module java.base;
import com.github.kjetilv.uplift.util.Bytes;

/// Maintains a simple pool of non-trivially-created instances of [MessageDigest], in the form of a queue.
/// On demand, query the queue for a digest, or create a new one.  On release, offer the digest to the queue.
///
/// @param <H>
final class MessageByteDigest<H extends HashKind<H>> implements ByteDigest<H> {

    static <H extends HashKind<H>> MessageByteDigest<H> get(H kind) {
        return new MessageByteDigest<>(kind);
    }

    private final H kind;

    private final Lock digestLock = new ReentrantLock();

    private MessageDigest messageDigest;

    MessageByteDigest(H kind) {
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    @Override
    public H kind() {
        return kind;
    }

    @Override
    public void digest(Bytes bs) {
        currentDigest().update(bs.bytes(), bs.offset(), bs.length());
    }

    /// Drain current digest, unset it and offer it to the queue.
    ///
    /// @return Hash of current digest
    @Override
    public Hash<H> get() {
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

    private static <H extends HashKind<H>> MessageDigest createDigest(H kind) {
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
