package com.github.kjetilv.uplift.json;

import java.util.Optional;

public interface BytesSource {

    int chomp();

    byte[] lexeme();

    byte[] spoolField();

    byte[] spoolString();

    byte[] spoolNumber();

    void skip4(byte c0, byte c1, byte c2);

    void skip5(byte c0, byte c1, byte c2, byte c3);

    boolean done();

    default Optional<String> pos() {
        return Optional.empty();
    }
}
