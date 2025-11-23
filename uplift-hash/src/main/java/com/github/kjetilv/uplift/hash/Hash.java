package com.github.kjetilv.uplift.hash;

import module java.base;
import com.github.kjetilv.uplift.util.Bytes;

import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static com.github.kjetilv.uplift.hash.HashKind.K256;
import static com.github.kjetilv.uplift.hash.StrUtils.*;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Objects.requireNonNull;

/// A hash, of a certain {@link HashKind}.
///
/// Can provide {@link #digest() digest}, which is a compact string representation. These can be parsed
/// back to hash instances with a factory method in {@link Hash#from(String) Hashes}.
public sealed interface Hash<H extends HashKind<H>> extends Comparable<Hash<H>> {

    static <H extends HashKind<H>> String toShortHashString(List<Hash<H>> hashes) {
        return hashes.stream()
            .map(Hash::toShortString)
            .collect(Collectors.joining("—"));
    }

    static Hash<K128> of(long long0, long long1) {
        return new H128(long0, long1);
    }

    static Hash<K256> of(long long0, long long1, long long2, long long3) {
        return new H256(long0, long1, long2, long3);
    }

    @SuppressWarnings("unchecked")
    static <H extends HashKind<H>> Hash<H> from(byte[] bytes) {
        requireNonNull(bytes, "bytes");
        if (bytes.length == K128.byteCount()) {
            var ls = Bytes.toLongs(bytes, new long[K128.longCount()]);
            return (Hash<H>) new H128(ls[0], ls[1]);
        }
        if (bytes.length == K256.byteCount()) {
            var ls = Bytes.toLongs(bytes, new long[K256.longCount()]);
            return (Hash<H>) new H256(ls[0], ls[1], ls[2], ls[3]);
        }
        throw new IllegalStateException("Byte size for hash not recognized: " + bytes.length + " bytes");
    }

    static Hash<K128> fromUUID(String uuidString) {
        var uuid = UUID.fromString(uuidString);
        return of(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    @SuppressWarnings("unchecked")
    static <H extends HashKind<H>> Hash<H> from(String raw) {
        var length = requireNonNull(raw, "raw").length();
        if (length == K128.digestLength()) {
            var ls = toLongs(raw, new long[K128.longCount()]);
            return (Hash<H>) new H128(ls[0], ls[1]);
        }
        if (length == K256.digestLength()) {
            var ls = toLongs(raw, new long[K256.longCount()]);
            return (Hash<H>) new H256(ls[0], ls[1], ls[2], ls[3]);
        }
        throw new IllegalArgumentException("Malformed hash of length " + length + " not recognized: " + raw);
    }

    @SuppressWarnings("unchecked")
    static <H extends HashKind<H>> Optional<Hash<H>> maybe(String raw) {
        var length = requireNonNull(raw, "raw").length();
        if (length == K128.digestLength()) {
            var ls = toLongs(raw, new long[K128.longCount()]);
            return Optional.of((Hash<H>) new H128(ls[0], ls[1]));
        }
        if (length == K256.digestLength()) {
            var ls = toLongs(raw, new long[K256.longCount()]);
            return Optional.of((Hash<H>) new H256(ls[0], ls[1], ls[2], ls[3]));
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    static <H extends HashKind<H>> Hash<H> of(DataInput input, H kind) {
        try {
            return (Hash<H>) switch (kind) {
                case K128 _ -> of(input.readLong(), input.readLong());
                case K256 _ -> of(
                    input.readLong(),
                    input.readLong(),
                    input.readLong(),
                    input.readLong()
                );
            };
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read from " + input, e);
        }
    }

    /// @return Unique string representation
    default String digest() {
        var bytes = Bytes.longsToBytes(ls());
        var base64 = new String(Base64.getEncoder().encode(bytes), US_ASCII);
        return normalize(base64, kind().digestLength());
    }

    default Bytes toBytes() {
        return Bytes.from(bytes());
    }

    /// @return Byte representation of the id
    default byte[] bytes() {
        var ls = ls();
        var bytes = new byte[ls.length * 8];
        for (var l = 0; l < ls.length; l++) {
            for (var i = 0; i < 8; i++) {
                bytes[l * 8 + i] = (byte) (ls[l] >>> 8 * (7 - i));
            }
        }
        return bytes;
    }

    default String toShortString() {
        return par(digest().substring(0, 6));
    }

    default String toLongString() {
        return par(digest());
    }

    @Override
    default int compareTo(Hash<H> o) {
        if (equals(o)) {
            return 0;
        }
        if (o.getClass() == getClass()) {
            return Arrays.compare(ls(), o.ls());
        }
        throw new ClassCastException(this + " is not comparable to " + o);
    }

    default boolean isBlank() {
        for (var l : ls()) {
            if (l != 0) {
                return false;
            }
        }
        return true;
    }

    default byte byteAt(int index) {
        return bytes()[index];
    }

    default String toStringCustom(int length) {
        if (length < 3) {
            throw new IllegalArgumentException(this + ": Invalid length: " + length + ", should be >= 2");
        }
        if (length > kind().digestLength() + 2) {
            throw new IllegalArgumentException(this + ": Invalid length: " + length + ", should <= " + kind().digestLength() + 2);
        }
        return par(digest().substring(0, length - 2));
    }

    default String defaultToString() {
        return par(digest().substring(0, Math.max(10, kind().digestLength() / 5)));
    }

    /// @return Standard Java {@link UUID#toString() UUID}, or fails if this is not a {@link H128 128-bit} hash
    /// @throws IllegalStateException If this is not a {@link H128 128-bit} hash
    default UUID asUuid() {
        return switch (this) {
            case H128(long l0, long l1) -> new UUID(l0, l1);
            case H256 _ -> throw new IllegalStateException("Not a valid UUID: " + this);
        };
    }

    default H kind() {
        return (H) switch (this) {
            case H128 _ -> K128;
            case H256 _ -> K256;
        };
    }

    /// The longs
    ///
    /// @return Longs
    long[] ls();

    private static long[] toLongs(String raw, long[] ls) {
        var digest = denormalize(raw);
        var decoded = Base64.getDecoder().decode(digest);
        for (var l = 0; l < ls.length; l++) {
            ls[l] = Bytes.bytesToLong(decoded, l * 8);
        }
        return ls;
    }

    record H128(long long0, long long1) implements Hash<K128> {

        @Override
        public long[] ls() {
            return new long[] {long0, long1};
        }

        @Override
        public String toString() {
            return defaultToString();
        }
    }

    record H256(long long0, long long1, long long2, long long3) implements Hash<K256> {

        @Override
        public long[] ls() {
            return new long[] {long0, long1, long2, long3};
        }

        @Override
        public String toString() {
            return defaultToString();
        }
    }
}
