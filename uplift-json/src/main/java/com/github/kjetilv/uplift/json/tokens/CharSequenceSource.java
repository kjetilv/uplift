package com.github.kjetilv.uplift.json.tokens;

import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.IntSupplier;

public final class CharSequenceSource extends AbstractBytesSource {

    public CharSequenceSource(CharSequence sequence) {
        super(reader(sequence));
    }

    private static IntSupplier reader(CharSequence sequence) {
        return new CharSeq(sequence);
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
                    return (int) sequence.charAt(index);
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to read from " + sequence, e);
                } finally {
                    this.index.increment();
                }
            }
            return -1;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" +index + "/" + length + "]";
        }
    }
}
