package com.github.kjetilv.uplift.hash;

import java.security.MessageDigest;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Maintains a simple pool of non-trivially-created instances of {@link MessageDigest}, in the form of a queue.
 * On demand, query the queue for a digest, or create a new one.  On release, offer the digest to the queue.
 *
 * @param <H>
 */
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

    @Override
    public Hash<H> get() {
        digestLock.lock();
        try {
            if (messageDigest == null) {
                return kind.blank();
            }
            try {
                return Hashes.hash(messageDigest.digest());
            } finally {
                MessageDigest discardable = messageDigest;
                messageDigest = null;
                queue(kind).offer(discardable);
            }
        } finally {
            digestLock.unlock();
        }
    }

    private MessageDigest currentDigest() {
        digestLock.lock();
        try {
            if (messageDigest == null) {
                MessageDigest pooled = queue(kind).poll();
                return messageDigest = pooled != null
                    ? pooled
                    : createDigest(kind);
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
            throw new IllegalStateException("Expected " + kind + " implementation", e);
        }
    }

    private static Deque<MessageDigest> queue(HashKind<?> kind) {
        return QUEUE.computeIfAbsent(kind, newPool());
    }

    private static Function<HashKind<?>, Deque<MessageDigest>> newPool() {
        return _ ->
            new LinkedBlockingDeque<>(DIGEST_POOL_SIZE);
    }
}
