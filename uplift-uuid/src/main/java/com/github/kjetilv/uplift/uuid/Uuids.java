package com.github.kjetilv.uplift.uuid;

import java.util.Base64;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.util.Objects.requireNonNull;

final class Uuids {

    static String digest(UUID uuid) {
        byte[] bytes = new byte[UUID_BYTES];
        byte[] most = longToBytes(uuid.getMostSignificantBits());
        System.arraycopy(most, 0, bytes, 0, 8);
        byte[] least = longToBytes(uuid.getLeastSignificantBits());
        System.arraycopy(least, 0, bytes, 8, 8);
        String hash = new String(ENCODER.encode(bytes), ISO_8859_1);
        if (hash.endsWith("==")) {
            return hash
                .substring(0, hash.length() - 2)
                .replace('/', '-')
                .replace('+', '_');
        }
        throw new IllegalStateException("Unusual hash: " + hash);
    }

    static UUID uuid(String digest) {
        requireNonNull(digest, "digest");
        int length = digest.length();
        if (length < Uuid.DIGEST_LENGTH) {
            throw new IllegalArgumentException("Malformed: " + digest);
        }
        byte[] digestChars = digest.substring(0, DIGEST_LENGTH).getBytes(ISO_8859_1);
        byte[] bytes = new byte[Uuid.DIGEST_LENGTH + 2];
        bytes[Uuid.DIGEST_LENGTH] = '=';
        bytes[Uuid.DIGEST_LENGTH + 1] = '=';
        for (int i = 0; i < Uuid.DIGEST_LENGTH; i++) {
            byte c = digestChars[i];
            bytes[i] = switch (c) {
                case '-' -> '/';
                case '_' -> '+';
                default -> c;
            };
        }
        byte[] decode = DECODER.decode(bytes);
        long most = bytesToLong(decode, 0);
        long least = bytesToLong(decode, 8);
        return new UUID(most, least);
    }

    private Uuids() {

    }

    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    private static final Base64.Decoder DECODER = Base64.getDecoder();

    private static final int MASK = 0xFF;

    private static final int UUID_BYTES = 16;

    private static final int DIGEST_LENGTH = 22;

    @SuppressWarnings("NumericCastThatLosesPrecision")
    private static byte[] longToBytes(long l) {
        long lw = l;
        byte[] bytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) (lw & MASK);
            lw >>= 8;
        }
        return bytes;
    }

    private static long bytesToLong(byte[] bytes, int start) {
        long lw = 0;
        for (int i = 0; i < 8; i++) {
            lw <<= 8;
            lw |= bytes[i + start] & MASK;
        }
        return lw;
    }
}
