package com.github.kjetilv.uplift.json.bytes;

import java.io.InputStream;
import java.util.Objects;
import java.util.function.IntSupplier;

public final class InputStreamIntsBytesSource extends AbstractIntsBytesSource {

    public InputStreamIntsBytesSource(InputStream stream) {
        super(new Ints(Objects.requireNonNull(stream, "stream")));
    }

    private record Ints(InputStream stream) implements IntSupplier {

        @Override
        public int getAsInt() {
            try {
                return stream.read();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read from " + stream, e);
            }
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + stream + "]";
        }
    }
}
