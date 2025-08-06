package com.github.kjetilv.uplift.kernel.io;

import static java.nio.charset.StandardCharsets.UTF_8;

public record Bytes(byte[] bytes, int offset, int length) {

    public static Bytes from(byte[] bytes) {
        return new Bytes(bytes);
    }

    public Bytes(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    public String string() {
        return new String(bytes, offset, length, UTF_8);
    }
}
