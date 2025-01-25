package com.github.kjetilv.uplift.json;

import com.github.kjetilv.flopp.kernel.LineSegment;

@FunctionalInterface
public interface TokenResolver {

    Token.Field get(LineSegment segment);
}
