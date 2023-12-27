package com.github.kjetilv.uplift.json.tokens;

import java.io.InputStream;

import static java.util.Objects.requireNonNull;

class BytesSource implements Source {

    private final InputStream stream;

    private final Progress progress;

    private int next1;

    private int next2;

    @SuppressWarnings("StringBufferField")
    private StringBuilder currentLexeme = new StringBuilder();

    BytesSource(InputStream stream) {
        this.stream = requireNonNull(stream, "stream");
        this.progress = new Progress();
        this.next1 = nextChar();
        this.next2 = next1 < 0 ? -1 : nextChar();
    }

    @Override
    public String lexeme(boolean quoted) {
        return quoted ? currentLexeme.substring(1, currentLexeme.length() - 1)
            : currentLexeme.length() == 1 ? Canonical.string(currentLexeme.charAt(0))
                : currentLexeme.toString();
    }

    @Override
    public char chomp() {
        char chomped = progress.chomped(toChar(next1));
        currentLexeme.append(chomped);
        next1 = next2;
        if (next1 >= 0) {
            next2 = nextChar();
        }
        return chomped;
    }

    @Override
    public int line() {
        return progress.line();
    }

    @Override
    public int column() {
        return progress.column();
    }

    @Override
    public char peek() {
        return toChar(next1);
    }

    @Override
    public char peekNext() {
        return toChar(next2);
    }

    @Override
    public void advance() {
        chomp();
    }

    @Override
    public void reset() {
        currentLexeme = new StringBuilder();
    }

    @Override
    public boolean done() {
        return next1 < 0;
    }

    private int nextChar() {
        try {
            return this.stream.read();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read from " + stream, e);
        }
    }

    private static char toChar(int returned) {
        if (returned < 0) {
            return '\0';
        }
        if (returned > Character.MAX_VALUE) {
            throw new IllegalStateException("Invalid char: " + returned);
        }
        return (char) returned;
    }
}
