package com.github.kjetilv.uplift.json.tokens;

import java.io.Reader;
import java.util.function.IntSupplier;

final class CharsSource extends AbstractBytesSource {

    CharsSource(Reader reader) {
        super(reader(reader));
    }

    private static IntSupplier reader(Reader reader) {
        return () -> {
            try {
                return reader.read();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read from " + reader, e);
            }
        };
    }
}
