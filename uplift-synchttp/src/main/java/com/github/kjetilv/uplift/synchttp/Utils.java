package com.github.kjetilv.uplift.synchttp;

import module java.base;
import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class Utils {

    public static final VectorSpecies<Byte> BYTE_SPECIES = VectorSpecies.ofPreferred(byte.class);

    public static final int BYTE_VECTOR_LENGTH = BYTE_SPECIES.length();

    public static final ByteOrder BYTE_ORDER = ByteOrder.nativeOrder();

    public static String string(MemorySegment memorySegment, long offset, long length) {
        return new String(
            memorySegment.asSlice(offset, length).toArray(JAVA_BYTE),
            UTF_8
        );
    }

    /// @return Offset of the first `b` in `[offset, offset + length)`, or -1
    public static int indexOf(byte b, MemorySegment segment, int offset, int length) {
        var end = offset + length;
        var walker = offset;
        for (; walker + BYTE_VECTOR_LENGTH <= end; walker += BYTE_VECTOR_LENGTH) {
            var position = vectorFrom(segment, walker)
                .compare(VectorOperators.EQ, b)
                .firstTrue();
            if (position < BYTE_VECTOR_LENGTH) {
                return walker + position;
            }
        }
        for (; walker < end; walker++) {
            if (segment.get(JAVA_BYTE, walker) == b) {
                return walker;
            }
        }
        return -1;
    }

    /// Decodes a URL query component: `%XX` escapes, and `+` as space. Malformed escapes are kept as they are.
    ///
    /// @return Decoded string, from UTF-8
    public static String decode(MemorySegment segment, long offset, long length) {
        var start = Math.toIntExact(offset);
        var count = Math.toIntExact(length);
        if (indexOf((byte) '%', segment, start, count) < 0 && indexOf((byte) '+', segment, start, count) < 0) {
            return string(segment, offset, length);
        }
        var bytes = segment.asSlice(offset, length).toArray(JAVA_BYTE);
        var decoded = new byte[bytes.length];
        var size = 0;
        for (var i = 0; i < bytes.length; i++) {
            var b = bytes[i];
            if (b == '+') {
                decoded[size++] = ' ';
            } else if (b == '%' && i + 2 < bytes.length && hex(bytes[i + 1]) >= 0 && hex(bytes[i + 2]) >= 0) {
                decoded[size++] = (byte) (hex(bytes[i + 1]) << 4 | hex(bytes[i + 2]));
                i += 2;
            } else {
                decoded[size++] = b;
            }
        }
        return new String(decoded, 0, size, UTF_8);
    }

    /// @return True if the range contains `%` or `+`, which [#decode(MemorySegment, long, long)] would change
    public static boolean encoded(MemorySegment segment, long offset, long length) {
        var start = Math.toIntExact(offset);
        var count = Math.toIntExact(length);
        return indexOf((byte) '%', segment, start, count) >= 0 || indexOf((byte) '+', segment, start, count) >= 0;
    }

    public static boolean prefixed(String prefix, MemorySegment segment, long offset) {
        var bytes = prefix.getBytes(UTF_8);
        return prefixed(segment, offset, bytes);
    }

    public static boolean didRead(ReadableByteChannel body, ByteBuffer buffer) {
        try {
            var read = body.read(buffer);
            if (read > 0) {
                buffer.flip();
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write body " + body, e);
        }
    }

    public static boolean isEqual(String value, MemorySegment segment, long offset) {
        var bytes = value.getBytes(UTF_8);
        if (bytes.length == value.length()) {
            return prefixed(segment, offset, bytes);
        }
        return false;
    }

    public static ByteVector vectorFrom(MemorySegment segment, long offset) {
        return ByteVector.fromMemorySegment(BYTE_SPECIES, segment, offset, BYTE_ORDER);
    }

    private Utils() {
    }

    private static int hex(byte b) {
        return Character.digit(b, 16);
    }

    private static boolean prefixed(MemorySegment segment, long offset, byte[] bytes) {
        for (var i = 0; i < bytes.length; i++) {
            if (segment.get(JAVA_BYTE, offset + i) != bytes[i]) {
                return false;
            }
        }
        return true;
    }
}
