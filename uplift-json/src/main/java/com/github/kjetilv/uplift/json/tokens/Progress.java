package com.github.kjetilv.uplift.json.tokens;

final class Progress {

    private int line = 1;

    private int column = 1;

    final int line() {
        return line;
    }

    final int column() {
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
}
