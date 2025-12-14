package com.github.kjetilv.uplift.util;

import module java.base;

import static java.nio.charset.StandardCharsets.UTF_8;

/// Describes a section of a byte array
public record Bytes(byte[] bytes, int offset, int length) {

    public static Bytes from(byte[] bytes) {
        return new Bytes(bytes);
    }

    public static Bytes from(String string) {
        return new Bytes(string.getBytes(UTF_8));
    }

    public static Bytes intBytes(int i) {
        return intToBytes(i, 0, new byte[Integer.BYTES]);
    }

    public static Bytes longBytes(long l) {
        return longToBytes(l, 0, new byte[Long.BYTES]);
    }

    public static Function<Integer, Bytes> intToBytes() {
        return Bytes::intBytes;
    }

    public static Bytes from(BigInteger bigInteger) {
        return from(bigInteger.toByteArray());
    }

    public static Bytes from(BigDecimal bigDecimal) {
        return from(bigDecimal.unscaledValue().toByteArray());
    }

    public static Bytes from(byte b) {
        return from(new byte[] {b});
    }

    public static Bytes from(Byte b) {
        return from(new byte[] {b});
    }

    public static long bytesToLong(byte[] bytes, int start) {
        long lw = 0;
        for (var i = 0; i < 7; i++) {
            lw |= bytes[i + start] & 0xFF;
            lw <<= 8;
        }
        lw |= bytes[7 + start] & 0xFF;
        return lw;
    }

    public static byte[] longsToBytes(long[] longs) {
        var bytes = new byte[longs.length * Long.BYTES];
        for (var l = 0; l < longs.length; l++) {
            longToBytes(longs[l], l * 8, bytes);
        }
        return bytes;
    }

    public static long[] toLongs(byte[] bytes, long[] ls) {
        for (var i = 0; i < 8; i++) {
            for (var j = 0; j < ls.length; j++) {
                ls[j] <<= 8;
                ls[j] |= bytes[i + j * 8] & 0xFF;
            }
        }
        return ls;
    }

    @SuppressWarnings("SameParameterValue")
    static Bytes intToBytes(int l, int index, byte[] bytes) {
        long w = l;
        for (var j = 3; j > 0; j--) {
            bytes[index + j] = (byte) (w & 0xFF);
            w >>= 8;
        }
        bytes[index] = (byte) (w & 0xFF);
        return new Bytes(bytes);
    }

    static Bytes longBytes(long long0, long long1, long long2, long long3) {
        var bytes = new byte[32];
        longToBytes(long0, 0, bytes);
        longToBytes(long1, 8, bytes);
        longToBytes(long2, 16, bytes);
        longToBytes(long3, 24, bytes);
        return from(bytes);
    }

    static byte[] longBytes(long long0, long long1) {
        var bytes = new byte[16];
        longToBytes(long0, 0, bytes);
        longToBytes(long1, 8, bytes);
        return bytes;
    }

    static Bytes longToBytes(long i, int index, byte[] bytes) {
        var w = i;
        for (var j = 7; j > 0; j--) {
            bytes[index + j] = (byte) (w & 0xFF);
            w >>= 8;
        }
        bytes[index] = (byte) (w & 0xFF);
        return from(bytes);
    }

    public Bytes(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    public String string() {
        return new String(bytes, offset, length, UTF_8);
    }

    public byte[] copyBytes() {
        return Arrays.copyOf(bytes, length);
    }
}
