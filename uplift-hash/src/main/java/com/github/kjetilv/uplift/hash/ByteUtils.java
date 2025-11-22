package com.github.kjetilv.uplift.hash;

import java.util.Base64;

final class ByteUtils {

    static long bytesToLong(byte[] bytes, int start) {
        long lw = 0;
        for (var i = 0; i < 7; i++) {
            lw |= bytes[i + start] & 0xFF;
            lw <<= 8;
        }
        lw |= bytes[7 + start] & 0xFF;
        return lw;
    }

    static byte[] longBytes(long long0, long long1, long long2, long long3) {
        var bytes = new byte[32];
        longToBytes(long0, 0, bytes);
        longToBytes(long1, 8, bytes);
        longToBytes(long2, 16, bytes);
        longToBytes(long3, 24, bytes);
        return bytes;
    }

    static byte[] longBytes(long long0, long long1) {
        var bytes = new byte[16];
        longToBytes(long0, 0, bytes);
        longToBytes(long1, 8, bytes);
        return bytes;
    }

    static byte[] longsToBytes(long[] longs) {
        var bytes = new byte[longs.length * Long.BYTES];
        for (var l = 0; l < longs.length; l++) {
            longToBytes(longs[l], l * 8, bytes);
        }
        return bytes;
    }

    static byte[] longToBytes(long i, int index, byte[] bytes) {
        var w = i;
        for (var j = 7; j > 0; j--) {
            bytes[index + j] = (byte) (w & 0xFF);
            w >>= 8;
        }
        bytes[index] = (byte) (w & 0xFF);
        return bytes;
    }

    @SuppressWarnings("SameParameterValue")
    static byte[] intToBytes(int l, int index, byte[] bytes) {
        long w = l;
        for (var j = 3; j > 0; j--) {
            bytes[index + j] = (byte) (w & 0xFF);
            w >>= 8;
        }
        bytes[index] = (byte) (w & 0xFF);
        return bytes;
    }

    static long[] toLongs(byte[] bytes, long[] ls) {
        for (var i = 0; i < 8; i++) {
            for (var j = 0; j < ls.length; j++) {
                ls[j] <<= 8;
                ls[j] |= bytes[i + j * 8] & 0xFF;
            }
        }
        return ls;
    }

    static long[] toLongs(String raw, long[] ls) {
        var digest = raw
            .replace(Hashes.GOOD_1, Hashes.BAD_1)
            .replace(Hashes.GOOD_2, Hashes.BAD_2);
        var decoded = Base64.getDecoder().decode(digest);
        for (var l = 0; l < ls.length; l++) {
            ls[l] = bytesToLong(decoded, l * 8);
        }
        return ls;
    }

    private ByteUtils() {
    }
}
