package com.github.kjetilv.uplift.json.ffm;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.io.BytesSupplier;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.AbstractJsonReader;
import com.github.kjetilv.uplift.json.tokens.IntsSource;
import com.github.kjetilv.uplift.json.Source;

import java.util.function.Consumer;
import java.util.function.Function;

public final class MemorySegmentJsonReader<T extends Record>
    extends AbstractJsonReader<LineSegment, T> {

    public MemorySegmentJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer) {
        super(callbacksInitializer);
    }

    @Override
    protected Source input(LineSegment lineSegment) {
        return new IntsSource(new BytesSupplier(lineSegment.longSupplier()));
    }
}
