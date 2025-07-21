package com.github.kjetilv.uplift.hash;

import java.nio.charset.StandardCharsets;

public record Bytes(byte[] bytes, int offset, int length) {

    public static Bytes from(byte[] bytes) {
        return new Bytes(bytes);
    }

    public Bytes(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    public String string() {
        return new String(bytes, offset, length, StandardCharsets.UTF_8);
    }
}
