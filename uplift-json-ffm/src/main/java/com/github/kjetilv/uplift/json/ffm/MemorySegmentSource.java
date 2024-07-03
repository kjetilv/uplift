package com.github.kjetilv.uplift.json.ffm;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.bits.Bits;
import com.github.kjetilv.flopp.kernel.bits.MemorySegments;
import com.github.kjetilv.uplift.json.tokens.AbstractBytesSource;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

public final class MemorySegmentSource extends AbstractBytesSource {

    public MemorySegmentSource(LineSegment lineSegment) {
        super(reader(lineSegment));
    }

    private static IntSupplier reader(LineSegment lineSegment) {
        return new LineSegmentIntSupplier(lineSegment);
    }

    private static final class LineSegmentIntSupplier implements IntSupplier {

        private final LongSupplier longSupplier;

        private int index;

        private long data;

        private LineSegmentIntSupplier(LineSegment lineSegment) {
            this.longSupplier = lineSegment.longSupplier();
            this.data = this.longSupplier.getAsLong();
        }

        @Override
        public int getAsInt() {
            try {
                return Bits.getByte(data, index);
            } finally {
                index++;
                if (index == MemorySegments.ALIGNMENT_INT) {
                    data = longSupplier.getAsLong();
                    index = 0;
                }
            }
        }
    }
}
