package com.github.kjetilv.uplift.json;

public interface Source {

    String lexeme();

    String quotedLexeme();

    char chomp();

    int line();

    int column();

    char peek();

    char peekNext();

    void reset();

    boolean done();
}
