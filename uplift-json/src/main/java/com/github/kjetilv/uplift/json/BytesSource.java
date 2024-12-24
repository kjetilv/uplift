package com.github.kjetilv.uplift.json;

import static java.nio.charset.StandardCharsets.UTF_8;

public interface BytesSource {

    void skip4(byte c0, byte c1, byte c2);

    void skip5(byte c0, byte c1, byte c2, byte c3);

    byte[] lexemeCopy();

    Loan lexemeLoan();

    void spoolField();

    void spoolString();

    void spoolNumber();

    byte chomp();

    void reset();

    boolean done();

    interface Loan {

        default String string() {
            return new String(loaned(), offset(), length(), UTF_8);
        }

        byte[] loaned();

        int offset();

        int length();
    }
}
