package com.github.kjetilv.uplift.json.tokens;

import java.io.InputStream;
import java.util.Objects;
import java.util.function.IntSupplier;

public final class InputStreamSource extends AbstractBytesSource {

    public InputStreamSource(InputStream stream) {
        super(new Ints(Objects.requireNonNull(stream, "stream")));
    }

    private static final class Ints implements IntSupplier {

        private final InputStream stream;

        private Ints(InputStream stream) {
            this.stream = Objects.requireNonNull(stream, "stream");
        }

        @Override
        public int getAsInt() {
            try {
                return Math.max(0, stream.read());
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
