package com.github.kjetilv.uplift.json.ffm;

import com.github.kjetilv.flopp.kernel.segments.LineSegment;
import com.github.kjetilv.flopp.kernel.util.BytesSupplier;
import com.github.kjetilv.uplift.json.tokens.AbstractBytesSource;

import java.util.function.IntSupplier;

public final class MemorySegmentSource extends AbstractBytesSource {

    public MemorySegmentSource(LineSegment lineSegment) {
        super(reader(lineSegment));
    }

    private static IntSupplier reader(LineSegment lineSegment) {
        return new BytesSupplier(lineSegment.longSupplier());
    }
}
