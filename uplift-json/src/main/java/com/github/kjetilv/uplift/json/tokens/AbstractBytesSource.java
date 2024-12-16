package com.github.kjetilv.uplift.json.tokens;

import com.github.kjetilv.uplift.json.Source;

import java.util.function.IntSupplier;

import static java.lang.Character.isDigit;

public abstract class AbstractBytesSource implements Source, Source.Loan {

    private char next1;

    private char next2;

    @SuppressWarnings("StringBufferField")
    private char[] currentLexeme = new char[1024];

    private int currentLexemeIndex;

    private final IntSupplier nextChar;

    public AbstractBytesSource(IntSupplier nextChar) {
        this.nextChar = nextChar;
        this.next1 = nextChar();
        this.next2 = next1 > 0 ? nextChar() : 0;
    }

    @Override
    public void spoolField() {
        reset();
        while (true) {
            try {
                switch (next1) {
                    case '"' -> {
                        return;
                    }
                    case 0 -> fail("Unterminated field");
                    case '\n' -> fail("Line break in field name: " + new String(lexeme()));
                    default -> {
                        added(next1);
                    }
                }
            } finally {
                next1 = next2;
                next2 = nextChar();

            }
        }
    }

    @Override
    public void spoolString() {
        reset();
        boolean quo = false;
        while (true) {
            char c = peek();
            switch (c) {
                case '"' -> {
                    if (quo) {
                        chomp();
                        quo = false;
                    } else {
                        advance();
                        return;
                    }
                }
                case '\\' -> {
                    if (quo) {
                        chomp();
                        quo = false;
                    } else {
                        advance();
                        quo = true;
                    }
                }   // -----------------------------
                case 0, // ------------/^^^^^\------
                     1, 2, // ---------|o   o|------
                     3, 4, 5, // -------\_._/-------
                     6, 7, 8, 9, // ----- \=--------
                     10, 11, 12, 13, // ---\_-------
                     14, 15, 16, 17, 18, // -\------
                     19, 20, 21, 22, 23, 24, // ----
                     25, 26, 27, 28, 29, 30, 31 -> {
                    if (quo) {
                        chomp();
                        quo = false;
                    } else {
                        fail("Unespaced control char: " + (int) c);
                    }
                }
                default -> {
                    quo = false;
                    chomp();
                }
            }
        }
    }

    @Override
    public void spoolNumber() {
        while (digital(peek())) {
            chomp();
        }
        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            do {
                chomp();
            } while (isDigit(peek()));
        }
    }

    @Override
    public char[] lexeme() {
        if (currentLexemeIndex == 1) {
            return Canonical.chars(currentLexeme[currentLexemeIndex - 1]);
        }
        char[] sub = new char[currentLexemeIndex];
        System.arraycopy(currentLexeme, 0, sub, 0, currentLexemeIndex);
        return sub;
    }

    @Override
    public Loan loanLexeme() {
        return this;
    }

    @Override
    public char chomp() {
        try {
            return added(next1);
        } finally {
            advance();
        }
    }

    @Override
    public void skip5(char c0, char c1, char c2, char c3) {
        assert next1 == c0 : next1 + "!=" + c0;
        assert next2 == c1 : next2 + "!=" + c1;
        int next3 = this.nextChar.getAsInt();
        assert next3 == c2 : next3 + "!=" + c2;
        int next4 = this.nextChar.getAsInt();
        assert next4 == c3 : next4 + "!=" + c3;
        next1 = nextChar();
        next2 = nextChar();
    }

    @Override
    public void skip4(char c0, char c1, char c2) {
        assert next1 == c0 : next1 + "!=" + c0;
        assert next2 == c1 : next2 + "!=" + c1;
        int next3 = this.nextChar.getAsInt();
        assert next3 == c2 : next3 + "!=" + c2;
        next1 = nextChar();
        next2 = nextChar();
    }

    private char peek() {
        return next1;
    }

    private char peekNext() {
        return next2;
    }

    @Override
    public void reset() {
        currentLexemeIndex = 0;
    }

    @Override
    public boolean done() {
        while (true) {
            if (next1 == 0) {
                return true;
            }
            if (!Character.isWhitespace(next1)) {
                return false;
            }
            advance();
        }
    }

    @Override
    public char[] loaned() {
        return currentLexeme;
    }

    @Override
    public int offset() {
        return 0;
    }

    @Override
    public int length() {
        return currentLexemeIndex;
    }

    private boolean digital(char peek) {
        return peek == '.' || isDigit(peek);
    }

    private void advance() {
        next1 = next2;
        next2 = nextChar();
    }

    private char added(char chomped) {
        try {
            currentLexeme[currentLexemeIndex] = chomped;
        } catch (ArrayIndexOutOfBoundsException ignore) {
            expand();
            currentLexeme[currentLexemeIndex] = chomped;
        } finally {
            currentLexemeIndex++;
        }
        return chomped;
    }

    private void expand() {
        char[] biggerLexeme = new char[currentLexemeIndex * 2];
        System.arraycopy(currentLexeme, 0, biggerLexeme, 0, currentLexemeIndex);
        currentLexeme = biggerLexeme;
    }

    private char nextChar() {
        return (char) this.nextChar.getAsInt();
    }

    private void fail(String msg) {
        throw new ReadException(msg, "`" + new String(lexeme()) + "`");
    }

    private static final int LN = 10;

    private static final int TAB = 9;

    private static final int CR = 13;

    private static final int BS = 8;

    private static String print(int c) {
        return switch (c) {
            case LN -> "\\n";
            case CR -> "\\r";
            case TAB -> "\\t";
            case BS -> "\\b";
            default -> Character.toString(c);
        };
    }

    @Override
    public String toString() {
        int tail = Math.min(LN, currentLexemeIndex);
        int printOffset = currentLexemeIndex - tail;
        return getClass().getSimpleName() + "[" + nextChar + " " +
               "<" + new String(currentLexeme, printOffset, tail) + ">" +
               "<" + print(next1) + print(next2) + ">]";
    }
}
