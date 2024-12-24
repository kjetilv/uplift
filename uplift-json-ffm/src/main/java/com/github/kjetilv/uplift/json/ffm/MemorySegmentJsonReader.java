package com.github.kjetilv.uplift.json.ffm;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.io.BytesSupplier;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.AbstractJsonReader;
import com.github.kjetilv.uplift.json.bytes.IntsBytesSource;
import com.github.kjetilv.uplift.json.BytesSource;

import java.util.function.Consumer;
import java.util.function.Function;

public final class MemorySegmentJsonReader<T extends Record>
    extends AbstractJsonReader<LineSegment, T> {

    public MemorySegmentJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer) {
        super(callbacksInitializer);
    }

    @Override
    protected BytesSource input(LineSegment lineSegment) {
        return new IntsBytesSource(new BytesSupplier(lineSegment.longSupplier()));
    }
}
