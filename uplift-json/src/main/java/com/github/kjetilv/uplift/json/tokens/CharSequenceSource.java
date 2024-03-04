package com.github.kjetilv.uplift.json.tokens;

import java.util.concurrent.atomic.LongAdder;
import java.util.function.IntSupplier;

final class CharSequenceSource extends AbstractBytesSource {

    CharSequenceSource(CharSequence sequence) {
        super(reader(sequence));
    }

    private static IntSupplier reader(CharSequence sequence) {
        int length = sequence.length();
        LongAdder i = new LongAdder();
        return () -> {
            int index = i.intValue();
            if (index < length) {
                try {
                    return (int) sequence.charAt(index);
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to read from " + sequence, e);
                } finally {
                    i.increment();
                }
            }
            return -1;
        };
    }

}
