package com.github.kjetilv.uplift.json.tokens;

import com.github.kjetilv.uplift.json.Source;

import java.util.Objects;
import java.util.function.IntSupplier;

public abstract class AbstractBytesSource implements Source {

    private final Progress progress;

    private char next1;

    private char next2;

    @SuppressWarnings("StringBufferField")
    private char[] currentLexeme = new char[1024];

    private int currentLexemeIndex;

    private final IntSupplier nextChar;

    public AbstractBytesSource(IntSupplier nextChar) {
        this.nextChar = Objects.requireNonNull(nextChar, "nextChar");
        this.next1 = (char) this.nextChar.getAsInt();
        this.next2 = next1 > 0 ? (char) this.nextChar.getAsInt() : 0;
        this.progress = new Progress();
    }

    @Override
    public String lexeme() {
        return currentLexemeIndex == 1 ? Canonical.string(currentLexeme[0])
            : new String(currentLexeme, 0, currentLexemeIndex);
    }

    @Override
    public String quotedLexeme() {
        return new String(currentLexeme, 1, currentLexemeIndex - 2);
    }

    @Override
    public char chomp() {
        char chomped = progress.chomped(next1);
        if (chomped == NULL_CHAR) {
            return NULL_CHAR;
        }
        add(chomped);
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
        return next1;
    }

    @Override
    public char peekNext() {
        return next2;
    }

    @Override
    public void reset() {
        currentLexemeIndex = 0;
    }

    @Override
    public boolean done() {
        return next1 == 0;
    }

    private void add(char chomped) {
        try {
            currentLexeme[currentLexemeIndex] = chomped;
        } catch (ArrayIndexOutOfBoundsException ignore) {
            expand();
            currentLexeme[currentLexemeIndex] = chomped;
        } finally {
            currentLexemeIndex++;
        }
    }

    private void expand() {
        char[] biggerLexeme = new char[currentLexemeIndex * 2];
        System.arraycopy(currentLexeme, 0, biggerLexeme, 0, currentLexemeIndex);
        currentLexeme = biggerLexeme;
    }

    private char nextChar() {
        return (char) this.nextChar.getAsInt();
    }

    private static final char NULL_CHAR = '\0';

    private static String print(int c) {
        return switch (c) {
            case (int) '\n' -> "\\n";
            case (int) '\t' -> "\\t";
            case (int) '\r' -> "\\r";
            case (int) '\b' -> "\\b";
            default -> "'" + (char) c + "'";
        };
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + nextChar + " " + print(next1) + "/" + print(next2) + "]";
    }
}
