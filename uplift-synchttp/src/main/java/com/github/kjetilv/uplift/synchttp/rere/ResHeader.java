package com.github.kjetilv.uplift.synchttp.rere;

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Objects;

public record ResHeader(String name, String value) {

    public static ResHeader contentLength(int contentLength) {
        return new ResHeader(CONTENT_LENGTH, String.valueOf(contentLength));
    }

    public ResHeader(String name, String value) {
        this.name = Objects.requireNonNull(name, "name").toLowerCase(Locale.ROOT);
        this.value = Objects.requireNonNull(value, "value");
    }

    public ByteBuffer buffer() {
        String line = "%s: %s\r\n".formatted(name, value);
        return ByteBuffer.wrap(line.getBytes());
    }

    public boolean isContentLength() {
        return name.equals(CONTENT_LENGTH);
    }

    private static final String CONTENT_LENGTH = "content-length";

    @Override
    public String toString() {
        return "%s: %s".formatted(name, value);
    }
}
