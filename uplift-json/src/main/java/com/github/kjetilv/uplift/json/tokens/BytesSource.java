package com.github.kjetilv.uplift.json.tokens;

import java.io.InputStream;
import java.util.function.IntSupplier;

public final class BytesSource extends AbstractBytesSource {

    public BytesSource(InputStream stream) {
        super(reader(stream));
    }

    private static IntSupplier reader(InputStream stream) {
        return () -> {
            try {
                return stream.read();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read from " + stream, e);
            }
        };
    }
}
