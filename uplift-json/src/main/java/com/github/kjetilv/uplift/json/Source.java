package com.github.kjetilv.uplift.json;

interface Source {

    default String lexeme() {
        return lexeme(false);
    }

    String lexeme(boolean quoted);

    char chomp();

    int line();

    int column();

    char peek();

    char peekNext();

    void advance();

    void reset();

    boolean done();
}
