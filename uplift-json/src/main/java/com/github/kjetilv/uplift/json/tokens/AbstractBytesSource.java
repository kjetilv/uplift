package com.github.kjetilv.uplift.json.tokens;

import com.github.kjetilv.uplift.json.Source;

import java.util.function.IntSupplier;

import static java.lang.Character.isDigit;

public abstract class AbstractBytesSource implements Source, Source.Loan {

    private byte next1;

    private byte next2;

    @SuppressWarnings("StringBufferField")
    private byte[] currentLexeme = new byte[1024];

    private int currentLexemeLength;

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
                    case '\n' -> fail("Line break in field name: " + new String(lexemeCopy()));
                    default -> added(next1);
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
            byte c = peek();
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
                    } // -----|   q|------------\__./=-
                } // =---------\_./--------------/-----
                case 0, // -----/-=----/^^^^^\--|------
                     1, 2, // ---------| q 'p|--|------
                     3, 4, 5, // -------\_._/---\------
                     6, 7, 8, 9, // ----- \=-------/^^^
                     10, 11, 12, 13, // ---\_-----|q` p
                     14, 15, 16, 17, 18, // -\----\_._/
                     19, 20, 21, 22, 23, 24, // ---/---
                     25, 26, 27, 28, 29, 30, 31 -> { //
                    if (quo) {
                        chomp();
                        quo = false;
                    } else {
                        fail("Unescaped control char: " + (int) c);
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
    public byte[] lexemeCopy() {
        if (currentLexemeLength == 1) {
            return Canonical.bytes(currentLexeme[currentLexemeLength - 1]);
        }
        byte[] sub = new byte[currentLexemeLength];
        System.arraycopy(currentLexeme, 0, sub, 0, currentLexemeLength);
        return sub;
    }

    @Override
    public Loan lexemeLoan() {
        return this;
    }

    @Override
    public byte chomp() {
        try {
            return added(next1);
        } finally {
            advance();
        }
    }

    @Override
    public void skip5(byte c0, byte c1, byte c2, byte c3) {
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
    public void skip4(byte c0, byte c1, byte c2) {
        assert next1 == c0 : next1 + "!=" + c0;
        assert next2 == c1 : next2 + "!=" + c1;
        int next3 = this.nextChar.getAsInt();
        assert next3 == c2 : next3 + "!=" + c2;
        next1 = nextChar();
        next2 = nextChar();
    }

    @Override
    public void reset() {
        currentLexemeLength = 0;
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
    public byte[] loaned() {
        return currentLexeme;
    }

    @Override
    public int offset() {
        return 0;
    }

    @Override
    public int length() {
        return currentLexemeLength;
    }

    private byte peek() {
        return next1;
    }

    private byte peekNext() {
        return next2;
    }

    private boolean digital(byte peek) {
        return peek == '.' || isDigit(peek);
    }

    private void advance() {
        next1 = next2;
        next2 = nextChar();
    }

    private byte added(byte chomped) {
        try {
            currentLexeme[currentLexemeLength] = chomped;
        } catch (ArrayIndexOutOfBoundsException ignore) {
            expand();
            currentLexeme[currentLexemeLength] = chomped;
        } finally {
            currentLexemeLength++;
        }
        return chomped;
    }

    private void expand() {
        byte[] biggerLexeme = new byte[currentLexemeLength * 2];
        System.arraycopy(currentLexeme, 0, biggerLexeme, 0, currentLexemeLength);
        currentLexeme = biggerLexeme;
    }

    private byte nextChar() {
        return (byte) this.nextChar.getAsInt();
    }

    private void fail(String msg) {
        throw new ReadException(msg, "`" + new String(lexemeCopy()) + "`");
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
        int tail = Math.min(LN, currentLexemeLength);
        int printOffset = currentLexemeLength - tail;
        return getClass().getSimpleName() + "[" + nextChar + " " +
               "<" + new String(currentLexeme, printOffset, tail) + ">" +
               "<" + print(next1) + print(next2) + ">]";
    }
}
