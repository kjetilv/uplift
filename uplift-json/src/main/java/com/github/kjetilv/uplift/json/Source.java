package com.github.kjetilv.uplift.json;

public interface Source {

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
            return new String(loaned(), offset(), length());
        }

        byte[] loaned();

        int offset();

        int length();
    }
}
