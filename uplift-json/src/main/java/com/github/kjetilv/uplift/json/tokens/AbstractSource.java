package com.github.kjetilv.uplift.json.tokens;

abstract class AbstractSource implements Source {

    private int line = 1;

    private int column = 1;

    @Override
    public final int line() {
        return line;
    }

    @Override
    public final int column() {
        return column;
    }

    final char chomped(char c) {
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    protected static String canonical(char c) {
        return switch (c) {
            case ':' -> ":";
            case ',' -> ",";
            case '{' -> "{";
            case '[' -> "[";
            case '}' -> "}";
            case ']' -> "]";
            default -> String.valueOf(c);
        };
    }
}
