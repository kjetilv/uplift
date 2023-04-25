package com.github.kjetilv.uplift.json;

import java.io.InputStream;

import static java.util.Objects.requireNonNull;

class BytesSource extends AbstractSource {

    private final InputStream stream;

    private int next1;

    private int next2;

    private int position = 0;

    @SuppressWarnings("StringBufferField")
    private StringBuilder currentLexeme = new StringBuilder();

    BytesSource(InputStream stream) {
        this.stream = requireNonNull(stream, "stream");
        try {
            this.next1 = stream.read();
            this.next2 = next1 < 0 ? -1 : stream.read();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read from " + stream, e);
        }
    }

    @Override
    public String lexeme(boolean quoted) {
        return quoted ? currentLexeme.substring(1, currentLexeme.length() - 1)
            : currentLexeme.length() == 1 ? canonical(currentLexeme.charAt(0))
                : currentLexeme.toString();
    }

    @Override
    public char chomp() {
        try {
            char chomped = chomped(toChar(next1));
            currentLexeme.append(chomped);
            next1 = next2;
            if (next1 >= 0) {
                try {
                    next2 = stream.read();
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to read from " + stream, e);
                }
            }
            return chomped;
        } finally {
            position++;
        }
    }

    @Override
    public int position() {
        return position;
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
