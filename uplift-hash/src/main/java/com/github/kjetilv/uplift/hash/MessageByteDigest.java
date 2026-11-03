package com.github.kjetilv.uplift.hash;

import module java.base;
import com.github.kjetilv.uplift.util.Bytes;

/// Digests bytes with a [MessageDigest]. Not thread-safe: belongs to one hash builder.
///
/// [MessageDigest] instances are not trivial to create. They are taken from a small shared pool when the first
/// bytes arrive, and returned when the hash is built. The pool is a fixed array of slots, accessed without
/// locks and without allocation.
///
/// @param <H> Hash kind
final class MessageByteDigest<H extends HashKind<H>> implements ByteDigest<H> {

    static <H extends HashKind<H>> MessageByteDigest<H> get(H kind) {
        return new MessageByteDigest<>(kind);
    }

    private final H kind;

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
    public void digest(ByteBuffer bytes) {
        currentDigest().update(bytes);
    }

    /// Drains the current digest and returns it to the pool.
    ///
    /// @return Hash of current digest, or the blank hash if nothing was digested
    @Override
    public Hash<H> get() {
        if (messageDigest == null) {
            return kind.blank();
        }
        var digest = messageDigest;
        messageDigest = null;
        try {
            return Hash.from(digest.digest());
        } finally {
            pool(kind).release(digest);
        }
    }

    private MessageDigest currentDigest() {
        if (messageDigest == null) {
            messageDigest = pool(kind).acquire();
        }
        return messageDigest;
    }

    private static final Pool POOL_128 = new Pool(HashKind.K128);

    private static final Pool POOL_256 = new Pool(HashKind.K256);

    /// Chosen by type: a map lookup would compute the record's hash code and equality on every call
    private static Pool pool(HashKind<?> kind) {
        return switch (kind) {
            case HashKind.K128 _ -> POOL_128;
            case HashKind.K256 _ -> POOL_256;
        };
    }

    @Override
    public String toString() {
        return getClass().getName() + "[" + kind + "]";
    }

    /// Fixed set of slots, each empty or holding an idle digest. Threads start at different slots.
    private static final class Pool {

        private final HashKind<?> kind;

        private final AtomicReferenceArray<MessageDigest> slots = new AtomicReferenceArray<>(SLOTS);

        private Pool(HashKind<?> kind) {
            this.kind = kind;
        }

        MessageDigest acquire() {
            var start = start();
            for (var i = 0; i < SLOTS; i++) {
                var slot = start + i & SLOTS - 1;
                if (slots.getPlain(slot) != null) {
                    var digest = slots.getAndSet(slot, null);
                    if (digest != null) {
                        return digest;
                    }
                }
            }
            return create();
        }

        void release(MessageDigest digest) {
            digest.reset();
            var start = start();
            for (var i = 0; i < SLOTS; i++) {
                var slot = start + i & SLOTS - 1;
                if (slots.getPlain(slot) == null && slots.compareAndSet(slot, null, digest)) {
                    return;
                }
            }
        }

        private MessageDigest create() {
            try {
                return MessageDigest.getInstance(kind.algorithm());
            } catch (Exception e) {
                throw new IllegalStateException("Expected " + kind.algorithm() + " implementation", e);
            }
        }

        private static int start() {
            return (int) Thread.currentThread().threadId() * 7 & SLOTS - 1;
        }

        private static final int SLOTS = 32;
    }
}
