package com.github.kjetilv.uplift.json;

import com.github.kjetilv.flopp.kernel.LineSegment;

public interface BytesSource {

    byte chomp();

    LineSegment lexeme();

    LineSegment spoolField();

    LineSegment spoolString();

    LineSegment spoolNumber();

    void skip4(byte c0, byte c1, byte c2);

    void skip5(byte c0, byte c1, byte c2, byte c3);

    boolean done();
}
