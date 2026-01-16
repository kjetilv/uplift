package com.github.kjetilv.uplift.asynchttp.rere;

import java.nio.ByteBuffer;

public record ResponseHeader(String name, String value) {

    public ByteBuffer buf() {
        String line = "%s: %s\n".formatted(name, value);
        return ByteBuffer.wrap(line.getBytes());
    }
}
