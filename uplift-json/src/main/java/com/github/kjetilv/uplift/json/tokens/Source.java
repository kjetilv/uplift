package com.github.kjetilv.uplift.json.tokens;

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

    void resetLexeme();

    boolean done();
}
