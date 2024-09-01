package com.github.kjetilv.uplift.json.tokens;

import java.util.Objects;
import java.util.function.IntSupplier;

public abstract class AbstractBytesSource implements Source {

    private final Progress progress;

    private int next1;

    private int next2;

    @SuppressWarnings("StringBufferField")
    private StringBuilder currentLexeme = new StringBuilder();

    private final IntSupplier nextChar;

    public AbstractBytesSource(IntSupplier nextChar) {
        this.nextChar = Objects.requireNonNull(nextChar, "nextChar");
        this.next1 = this.nextChar.getAsInt();
        this.next2 = next1 > 0 ? this.nextChar.getAsInt() : 0;
        this.progress = new Progress();
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
        if (chomped == NIL) {
            return NIL;
        }
        currentLexeme.append(chomped);
        next1 = next2;
        if (next1 > 0) {
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
        return next1 == 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + nextChar + " " + print(next1) + "/" + print(next2) + "]";
    }

    private int nextChar() {
        return this.nextChar.getAsInt();
    }

    private static final char NIL = '\0';

    private static final double MAX = Character.MAX_VALUE;

    private static char toChar(int returned) {
        if (returned <= 0) {
            return NIL;
        }
        if (returned > MAX) {
            throw new IllegalStateException("Invalid char: " + returned);
        }
        return (char) returned;
    }

    private static String print(int c) {
        return switch (c) {
            case (int)'\n' -> "\\n";
            case (int)'\t' -> "\\t";
            case (int)'\r' -> "\\r";
            case (int)'\b' -> "\\b";
            default -> "'" + (char)c + "'";
        };
    }
}
