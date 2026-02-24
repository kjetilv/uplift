package com.github.kjetilv.uplift.hash;

import module java.base;
import com.github.kjetilv.uplift.util.Bytes;

import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static com.github.kjetilv.uplift.hash.HashKind.K256;
import static com.github.kjetilv.uplift.hash.StrUtils.*;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Objects.requireNonNull;

/// A hash, of a certain [kind][HashKind].  Can be created with [128 bits][K128#of(long, long)] or
/// [256 bits][K256#of(long, long, long, long)].
///
/// Instances can provide a [digest][#digest()], which is a compact string representation.
/// These can be parsed back to hash instances with other [factory methods][Hash#from(String)],
public sealed interface Hash<K extends HashKind<K>> extends Comparable<Hash<K>> {

    static <K extends HashKind<K>> String toShortHashString(List<Hash<K>> hashes) {
        return hashes.stream()
            .map(Hash::toShortString)
            .collect(Collectors.joining("—"));
    }

    @SuppressWarnings("unchecked")
    static <K extends HashKind<K>> Hash<K> from(byte[] bytes) {
        requireNonNull(bytes, "bytes");
        if (bytes.length == K128.byteCount()) {
            var ls = Bytes.toLongs(bytes, new long[K128.longCount()]);
            return (Hash<K>) new H128(ls[0], ls[1]);
        }
        if (bytes.length == K256.byteCount()) {
            var ls = Bytes.toLongs(bytes, new long[K256.longCount()]);
            return (Hash<K>) new H256(ls[0], ls[1], ls[2], ls[3]);
        }
        throw new IllegalStateException("Byte size for hash not recognized: " + bytes.length + " bytes");
    }

    /// Parse a digest
    ///
    /// @param digest Digest
    /// @return Hash
    /// @throws IllegalArgumentException If the digest was not valid
    @SuppressWarnings("unchecked")
    static <K extends HashKind<K>> Hash<K> from(String digest) {
        var length = requireNonNull(digest, "digest").length();
        var k256Length = K256.digestLength();
        if (length >= k256Length) {
            var substring = length == k256Length ? digest : digest.substring(0, k256Length);
            var ls = toLongs(substring, new long[K256.longCount()]);
            return (Hash<K>) new H256(ls[0], ls[1], ls[2], ls[3]);
        }
        var k128Length = K128.digestLength();
        if (length >= k128Length) {
            var substring = length == k128Length ? digest : digest.substring(0, k128Length);
            var ls = toLongs(substring, new long[K128.longCount()]);
            return (Hash<K>) new H128(ls[0], ls[1]);
        }
        var reportedString = digest.substring(0, Math.min(length, 10)) + (length > 10 ? "..." : "");
        throw new IllegalArgumentException("Hash of length " + length + " not recognized: " + reportedString);
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
    default int compareTo(Hash<K> o) {
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

    /// @return Standard Java [UUID][UUID#toString()], or fails if this is not a [128-bit][H128] hash
    /// @throws IllegalStateException If this is not a [128-bit][H128] hash
    default UUID asUuid() {
        return switch (this) {
            case H128(var l0, var l1) -> new UUID(l0, l1);
            case H256 _ -> throw new IllegalStateException("Not a valid UUID: " + this);
        };
    }

    default K kind() {
        return (K) switch (this) {
            case H128 _ -> K128;
            case H256 _ -> K256;
        };
    }

    /// The longs
    ///
    /// @return Longs
    long[] ls();

    private static long[] toLongs(String raw, long[] ls) {
        try {
            var digest = denormalize(raw);
            var decoded = Base64.getDecoder().decode(digest);
            for (var l = 0; l < ls.length; l++) {
                ls[l] = Bytes.bytesToLong(decoded, l * 8);
            }
            return ls;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse " + raw, e);
        }
    }

    record H128(long long0, long long1) implements Hash<K128> {

        @Override
        public long[] ls() {
            return new long[] {long0, long1};
        }

        @Override
        public String toString() {
            return par(digest().substring(0, 8));
        }
    }

    record H256(long long0, long long1, long long2, long long3) implements Hash<K256> {

        @Override
        public long[] ls() {
            return new long[] {long0, long1, long2, long3};
        }

        @Override
        public String toString() {
            return par(digest().substring(0, 12));
        }
    }
}
