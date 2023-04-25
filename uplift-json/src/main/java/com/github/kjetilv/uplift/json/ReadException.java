package com.github.kjetilv.uplift.json;

class ReadException extends RuntimeException {

    private final String lexeme;

    private final int line;

    private final int column;

    ReadException(String msg, String lexeme, int line, int column, Throwable cause) {
        super(msg + ": `" + lexeme + "` [" + line + ":" + column + "]", cause);
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    String getLexeme() {
        return lexeme;
    }

    int getLine() {
        return line;
    }

    int getColumn() {
        return column;
    }
}
