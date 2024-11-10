package com.github.kjetilv.uplift.json;

public interface Source {

    default String lexeme() {
        return lexeme(false);
    }

    String lexeme(boolean quoted);

    char chomp();

    int line();

    int column();

    char peek();

    char peekNext();

    void reset();

    boolean done();
}
