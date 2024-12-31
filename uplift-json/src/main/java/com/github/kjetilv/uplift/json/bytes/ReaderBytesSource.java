package com.github.kjetilv.uplift.json.bytes;

import java.io.Reader;
import java.util.Objects;
import java.util.function.IntSupplier;

public final class ReaderBytesSource extends AbstractBytesSource {

    public ReaderBytesSource(Reader reader) {
        super(new Chars(Objects.requireNonNull(reader, "reader")));
    }

    private record Chars(Reader reader) implements IntSupplier {

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
