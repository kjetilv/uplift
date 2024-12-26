package com.github.kjetilv.uplift.json;

import com.github.kjetilv.flopp.kernel.LineSegment;

public interface TokenResolver {

    Token.Field get(LineSegment segment);
}
