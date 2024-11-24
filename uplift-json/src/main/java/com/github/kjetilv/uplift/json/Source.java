package com.github.kjetilv.uplift.json;

public interface Source {

    String lexeme();

    String quotedLexeme();

    char chomp();

    void skip(int chars);

    int line();

    int column();

    char peek();

    char peekNext();

    void reset();

    boolean done();
}
