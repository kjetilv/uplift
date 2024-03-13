package com.github.kjetilv.uplift.json.ffm;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.events.AbstractJsonReader;
import com.github.kjetilv.uplift.json.tokens.Source;

import java.util.function.Consumer;
import java.util.function.Function;

public class MemorySegmentJsonReader<T extends Record, C extends Callbacks>
    extends AbstractJsonReader<LineSegment, T, C> {

    protected MemorySegmentJsonReader(Function<Consumer<T>, C> callbacks) {
        super(callbacks);
    }

    @Override
    protected Source input(LineSegment source) {
        return new MemorySegmentSource(source.memorySegment(), source.startIndex(), source.endIndex());
    }
}
