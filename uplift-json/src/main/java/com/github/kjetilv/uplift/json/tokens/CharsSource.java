package com.github.kjetilv.uplift.json.tokens;

import java.io.Reader;
import java.util.Objects;
import java.util.function.IntSupplier;

public final class CharsSource extends AbstractBytesSource {

    public CharsSource(Reader reader) {
        super(new Chars(Objects.requireNonNull(reader, "reader")));
    }

    private static final class Chars implements IntSupplier {

        private final Reader reader;

        private Chars(Reader reader) {
            this.reader = reader;
        }

        @Override
        public int getAsInt() {
            try {
                return reader.read();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read from " + reader, e);
            }
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + reader + "]";
        }
    }
}
