package com.github.kjetilv.uplift.json;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.LineSegments;

import java.util.function.LongToIntFunction;

@FunctionalInterface
public interface TokenResolver {

    default Token.Field get(Token.Field token) {
        return get(token.lineSegment());
    }

    default Token.Field get(String token) {
        return get(LineSegments.of(token));
    }

    default Token.Field get(LineSegment segment) {
        return get(segment, 0, segment.length());
    }

    default Token.Field get(
        LineSegment segment,
        long offset,
        long length
    ) {
        return get(segment::byteAt, offset, length);
    }

    Token.Field get(LongToIntFunction get, long offset, long length);
}
