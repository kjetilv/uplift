package com.github.kjetilv.uplift.json.tokens;

import java.util.Objects;

public final class BytesSource extends AbstractBytesSource {

    public BytesSource(byte[] bytes) {
        super(new IntSupplier(Objects.requireNonNull(bytes, "bytes")));
    }

    private static class IntSupplier implements java.util.function.IntSupplier {

        private int i;

        private final int len;

        private final byte[] bytes;

        public IntSupplier(byte[] bytes) {
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
