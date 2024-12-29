package com.github.kjetilv.uplift.json;

import com.github.kjetilv.flopp.kernel.LineSegment;

public interface BytesSource {

    boolean done();

    byte chomp();

    LineSegment lexeme();

    void skip4(byte c0, byte c1, byte c2);

    void skip5(byte c0, byte c1, byte c2, byte c3);

    void spoolField();

    void spoolString();

    void spoolNumber();
}
