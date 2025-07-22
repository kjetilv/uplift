package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.bytes.LineSegmentBytesSource;

import java.util.function.Consumer;
import java.util.function.Function;

public final class LineSegmentJsonReader<T extends Record>
    extends AbstractJsonReader<LineSegment, T> {

    public LineSegmentJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer) {
        super(callbacksInitializer);
    }

    @Override
    protected BytesSource input(LineSegment lineSegment) {
        return new LineSegmentBytesSource(lineSegment);
    }
}
