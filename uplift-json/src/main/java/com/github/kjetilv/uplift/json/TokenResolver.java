package com.github.kjetilv.uplift.json;

import com.github.kjetilv.flopp.kernel.LineSegment;

@FunctionalInterface
public interface TokenResolver {

    default Token.Field get(LineSegment segment) {
        return get(segment, 0, segment.length());
    }

    Token.Field get(LineSegment segment, long offset, long length);
}
