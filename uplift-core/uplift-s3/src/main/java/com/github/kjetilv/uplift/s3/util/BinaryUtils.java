package com.github.kjetilv.uplift.s3.util;

import java.util.Locale;

/**
 * Utilities for encoding and decoding binary data to and from different forms.
 */
public final class BinaryUtils {

    /**
     * Converts byte data to a Hex-encoded string.
     *
     * @param data data to hex encode.
     *
     * @return hex-encoded string.
     */
    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte datum: data) {
            String hex = Integer.toHexString(datum);
            if (hex.length() == 1) {
                // Append leading zero.
                sb.append("0");
            } else if (hex.length() == 8) {
                // Remove ff prefix from negative numbers.
                hex = hex.substring(6);
            }
            sb.append(hex);
        }
        return sb.toString().toLowerCase(Locale.getDefault());
    }

    private BinaryUtils() {
    }
}
