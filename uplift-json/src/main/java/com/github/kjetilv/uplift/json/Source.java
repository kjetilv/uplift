package com.github.kjetilv.uplift.json;

public interface Source {

    void skip4(char c0, char c1, char c2);

    void skip5(char c0, char c1, char c2, char c3);

    char[] lexeme();

    Loan loanLexeme();

    void spoolField();

    void spoolString();

    void spoolNumber();

    char chomp();

    void skip();

    char peek();

    char peekNext();

    void reset();

    boolean done();

    interface Loan {

        default String string() {
            return new String(loaned(), 0, length());
        }

        char[] loaned();

        int length();
    }
}
