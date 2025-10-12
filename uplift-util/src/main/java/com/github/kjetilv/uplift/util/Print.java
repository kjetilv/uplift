package com.github.kjetilv.uplift.util;

public final class Print {

    public static String semiSecret(String semi) {
        var length = semi.length();
        if (length == 0) {
            return "<empty secret>";
        }
        if (length == 1) {
            return "*";
        }
        if (length < 5) {
            return semi.charAt(0) + "***".substring(0, length - 1);
        }
        var printable = length / 3;
        return semi.substring(0, printable) + "***" + semi.substring(length - printable);
    }

    @SuppressWarnings("MagicNumber")
    public static String bytes(long bytes) {
        if (bytes > 10 * M) {
            return String.format("%dM", bytes / M);
        }
        if (bytes > M) {
            if (bytes % M == 0) {
                return String.format("%dMiB", bytes / M);
            }
            return String.format("%.1fM", bytes * 10 / M / 10.0D);
        }
        if (bytes > K) {
            if (bytes % K == 0) {
                return String.format("%dKiB", bytes / K);
            }
            return String.format("%dk", bytes / K);
        }
        return String.format("%d", bytes);
    }

    private Print() {
    }

    private static final int K = 1_024;

    private static final int M = K * K;
}
