package com.github.kjetilv.uplift.json.tokens;

public class ReadException extends RuntimeException {

    private final String lexeme;

    private final int line;

    private final int column;

    ReadException(String msg, String lexeme, int line, int column, Throwable cause) {
        super(msg + ": `" + lexeme + "` [" + line + ":" + column + "]", cause);
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
