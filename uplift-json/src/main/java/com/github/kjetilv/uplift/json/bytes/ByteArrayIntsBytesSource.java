package com.github.kjetilv.uplift.json.bytes;

import java.util.Objects;
import java.util.function.IntSupplier;

public final class ByteArrayIntsBytesSource extends AbstractIntsBytesSource {

    public ByteArrayIntsBytesSource(byte[] bytes) {
        super(new Ints(Objects.requireNonNull(bytes, "bytes")));
    }

    private static class Ints implements IntSupplier {

        private int i;

        private final int len;

        private final byte[] bytes;

        public Ints(byte[] bytes) {
            this.bytes = bytes;
            i = 0;
            len = bytes.length;
        }

        @Override
        public int getAsInt() {
            return i == len ? 0 : bytes[i++];
        }
    }
}
