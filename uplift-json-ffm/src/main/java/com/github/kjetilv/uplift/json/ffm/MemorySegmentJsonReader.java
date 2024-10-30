package com.github.kjetilv.uplift.json.ffm;

import com.github.kjetilv.flopp.kernel.segments.LineSegment;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.events.AbstractJsonReader;
import com.github.kjetilv.uplift.json.tokens.Source;

import java.util.function.Consumer;
import java.util.function.Function;

public class MemorySegmentJsonReader<T extends Record>
    extends AbstractJsonReader<LineSegment, T> {

    protected MemorySegmentJsonReader(Function<Consumer<T>, Callbacks> callbacks) {
        super(callbacks);
    }

    @Override
    protected Source input(LineSegment lineSegment) {
        return new MemorySegmentSource(lineSegment);
    }
}
