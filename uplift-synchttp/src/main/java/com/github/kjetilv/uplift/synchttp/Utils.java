package com.github.kjetilv.uplift.synchttp;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class Utils {

    private static final VectorSpecies<Byte> BYTE_SPECIES = VectorSpecies.ofPreferred(byte.class);

    public static final int BYTE_VECTOR_LENGTH = BYTE_SPECIES.length();

    public static String string(MemorySegment memorySegment, long offset, long length) {
        return new String(
            memorySegment.asSlice(offset, length).toArray(JAVA_BYTE),
            UTF_8
        );
    }

    public static int indexOf(byte b, MemorySegment segment, int offset, int length) {
        var walker = offset;
        while (true) {
            var byteVector = vectorFrom(segment, walker);
            var vectorMask = byteVector.compare(VectorOperators.EQ, b);
            var position = vectorMask.firstTrue();
            if (position != BYTE_VECTOR_LENGTH) {
                return walker + position;
            }
            if (walker >= length) {
                return -1;
            }
            walker += BYTE_VECTOR_LENGTH;
        }
    }

    public static boolean prefixed(String prefix, MemorySegment segment, long offset) {
        var bytes = prefix.getBytes(UTF_8);
        return prefixed(segment, offset, bytes);
    }

    public static boolean readInto(ReadableByteChannel body, ByteBuffer buffer) {
        try {
            var read = body.read(buffer);
            if (read == -1) {
                return false;
            }
            buffer.flip();
            return true;
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

    public static VectorMask<Byte> unset(VectorMask<Byte> mask, int pos) {
        return mask.indexInRange(-pos, BYTE_VECTOR_LENGTH - pos);
    }

    private Utils() {
    }

    private static final ByteOrder BYTE_ORDER = ByteOrder.nativeOrder();

    private static boolean prefixed(MemorySegment segment, long offset, byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if (segment.get(JAVA_BYTE, offset + i) != bytes[i]) {
                return false;
            }
        }
        return true;
    }
}
