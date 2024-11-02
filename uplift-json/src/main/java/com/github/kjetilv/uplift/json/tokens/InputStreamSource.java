package com.github.kjetilv.uplift.json.tokens;

import java.io.InputStream;
import java.util.Objects;
import java.util.function.IntSupplier;

public final class InputStreamSource extends AbstractBytesSource {

    public InputStreamSource(InputStream stream) {
        super(reader(stream));
    }

    private static IntSupplier reader(InputStream stream) {
        return new Stream(stream);
    }

    private static final class Stream implements IntSupplier {

        private final InputStream stream;

        private Stream(InputStream stream) {
            this.stream = Objects.requireNonNull(stream, "stream");
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + stream + "]";
        }

        @Override
        public int getAsInt() {
            try {
                return Math.max(0, stream.read());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read from " + stream, e);
            }
        }
    }
}
