package com.github.kjetilv.uplift.json.tokens;

import java.io.InputStream;
import java.util.function.Supplier;

class BytesSource extends AbstractBytesSource {

    BytesSource(InputStream stream) {
        super(reader(stream));
    }

    private static Supplier<Integer> reader(InputStream stream) {
        return () -> {
            try {
                return stream.read();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read from " + stream, e);
            }
        };
    }
}
