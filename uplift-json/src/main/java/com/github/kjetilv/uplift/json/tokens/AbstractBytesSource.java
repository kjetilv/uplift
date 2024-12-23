package com.github.kjetilv.uplift.json.tokens;

import com.github.kjetilv.uplift.json.Source;

import java.util.function.IntSupplier;

import static java.lang.Character.isDigit;

public abstract class AbstractBytesSource implements Source, Source.Loan {

    private byte next1;

    private byte next2;

    @SuppressWarnings("StringBufferField")
    private byte[] currentLexeme = new byte[1024];

    private int index;

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
            if (next1 >> 5 == 0) {
                fail("Unescaped control char: " + (char) next1);
            }
            try {
                if (next1 == '"') {
                    return;
                }
                add(next1);
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
            if (c >> 5 == 0) {
                if (quo) {
                    chomp();
                    quo = false;
                } else {
                    fail("Unescaped control char: " + (int) c);
                }
                continue;
            }
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
        byte[] sub = new byte[index];
        System.arraycopy(currentLexeme, 0, sub, 0, index);
        return sub;
    }

    @Override
    public Loan lexemeLoan() {
        return this;
    }

    @Override
    public byte chomp() {
        try {
            add(next1);
            return next1;
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
        index = 0;
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
        return index;
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

    private void add(byte chomped) {
        try {
            currentLexeme[index] = chomped;
        } catch (ArrayIndexOutOfBoundsException ignore) {
            expand();
            currentLexeme[index] = chomped;
        } finally {
            index++;
        }
    }

    private void expand() {
        byte[] biggerLexeme = new byte[index * 2];
        System.arraycopy(currentLexeme, 0, biggerLexeme, 0, index);
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
        int tail = Math.min(LN, index);
        int printOffset = index - tail;
        return getClass().getSimpleName() + "[" + nextChar + " " +
               "<" + new String(currentLexeme, printOffset, tail) + ">" +
               "<" + print(next1) + print(next2) + ">]";
    }
}
