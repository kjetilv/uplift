package com.github.kjetilv.uplift.json.bytes;

import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.IntSupplier;

public final class CharSequenceBytesSource extends AbstractBytesSource {

    public CharSequenceBytesSource(CharSequence sequence) {
        super(new CharSeq(Objects.requireNonNull(sequence, "sequence")));
    }

    private static final class CharSeq implements IntSupplier {

        private final int length;

        private final LongAdder index;

        private final CharSequence sequence;

        private CharSeq(CharSequence sequence) {
            this.sequence = Objects.requireNonNull(sequence, "sequence");
            this.length = sequence.length();
            this.index = new LongAdder();
        }

        @Override
        public int getAsInt() {
            int index = this.index.intValue();
            if (index < length) {
                try {
                    return sequence.charAt(index);
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to read from " + sequence, e);
                } finally {
                    this.index.increment();
                }
            }
            return 0;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + index + "/" + length + "]";
        }
    }
}
