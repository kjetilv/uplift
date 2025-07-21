package com.github.kjetilv.uplift.hash;

import java.security.MessageDigest;
import java.util.Objects;

final class MessageByteDigest<H extends HashKind<H>> implements ByteDigest<H> {

    private final MessageDigest messageDigest;

    private final H kind;

    MessageByteDigest(H kind) {
        this.kind = Objects.requireNonNull(kind, "kind");
        try {
            this.messageDigest = MessageDigest.getInstance(this.kind.algorithm());
        } catch (Exception e) {
            throw new IllegalStateException("Expected " + this.kind + " implementation", e);
        }
    }

    @Override
    public H kind() {
        return kind;
    }

    @Override
    public void digest(Bytes bs) {
        messageDigest.update(bs.bytes(), bs.offset(), bs.length());
    }

    @Override
    public Hash<H> get() {
        return Hashes.hash(messageDigest.digest());
    }
}
