package com.github.kjetilv.uplift.s3.auth;

import module java.base;

public final class Hashes {

    /// Hashes the string contents (assumed to be UTF-8) using the SHA-256
    /// algorithm.
    public static byte[] sha256(String text) {
        return sha256(text.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] sha256(byte[] bytes) {
        try {
            MessageDigest md = SHA_256.get();
            md.update(bytes);
            return md.digest();
        } catch (Exception e) {
            throw new RuntimeException("Unable to compute hash while signing request: " + e.getMessage(), e);
        }
    }

    public static String md5(byte[] body) {
        MessageDigest md = MD5.get();
        md.update(body);
        byte[] digest = md.digest();
        return Base64.getEncoder().encodeToString(digest);
    }

    private Hashes() {
    }

    private static final ThreadLocal<MessageDigest> MD5 = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    });

    private static final ThreadLocal<MessageDigest> SHA_256 = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    });
}
