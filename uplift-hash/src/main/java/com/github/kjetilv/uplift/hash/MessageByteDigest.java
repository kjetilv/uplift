package com.github.kjetilv.uplift.hash;

import java.security.MessageDigest;
import java.util.Objects;

final class MessageByteDigest<K extends HashKind<K>> implements ByteDigest<K> {

    private final MessageDigest messageDigest;

    private final K kind;

    MessageByteDigest(K kind) {
        this.kind = Objects.requireNonNull(kind, "kind");
        try {
            this.messageDigest = MessageDigest.getInstance(this.kind.algorithm());
        } catch (Exception e) {
            throw new IllegalStateException("Expected " + this.kind + " implementation", e);
        }
    }

    @Override
    public K kind() {
        return kind;
    }

    @Override
    public void digest(Bytes bs) {
        messageDigest.update(bs.bytes(), bs.offset(), bs.length());
    }

    @Override
    public Hash<K> get() {
        return Hashes.hash(messageDigest.digest());
    }
}
