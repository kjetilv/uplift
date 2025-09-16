package com.github.kjetilv.uplift.json;

import com.github.kjetilv.uplift.util.Bytes;

public interface BytesSource {

    int chomp();

    Bytes lexeme();

    Bytes spoolField();

    Bytes spoolString();

    Bytes spoolNumber();

    void skip(char c1, char c2, char c3);

    void skip(char c1, char c2, char c3, char c4);

    boolean done();
}
