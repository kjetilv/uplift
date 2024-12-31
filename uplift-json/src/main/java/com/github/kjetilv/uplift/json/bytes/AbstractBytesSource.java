package com.github.kjetilv.uplift.json.bytes;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.LineSegments;
import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.io.ReadException;

import java.util.function.IntSupplier;

import static java.lang.Character.isDigit;

public abstract class AbstractBytesSource implements BytesSource {

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
    public LineSegment spoolField() {
        index = 0;
        while (true) {
            if (next1 >> 5 == 0) {
                fail("Unescaped control char: " + (char) next1);
            }
            try {
                if (next1 == '"') {
                    return lexeme();
                }
                add(next1);
            } finally {
                next1 = next2;
                next2 = nextChar();
            }
        }
    }

    @Override
    public LineSegment spoolString() {
        index = 0;
        boolean quo = false;
        while (true) {
            byte b = next1;
            if (b >> 5 == 0) {
                if (quo) {
                    save();
                    quo = false;
                } else {
                    fail("Unescaped control char: " + (int) b);
                }
                continue;
            }
            switch (b) {
                case '"' -> {
                    if (quo) {
                        save();
                        quo = false;
                    } else {
                        advance();
                        return lexeme();
                    }
                }
                case '\\' -> {
                    if (quo) {
                        save();
                        quo = false;
                    } else {
                        advance();
                        quo = true;
                    }
                }
                default -> {
                    quo = false;
                    save();
                }
            }
        }
    }

    @Override
    public LineSegment spoolNumber() {
        index++; // First digit is already in buffer
        while (digital(next1)) {
            save();
        }
        // Look for a fractional part.
        if (next1 == '.' && isDigit(next2)) {
            // Consume the "."
            do {
                save();
            } while (isDigit(next1));
        }
        return lexeme();
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
    public boolean done() {
        while (true) {
            switch (next1) {
                case 0 -> {
                    return true;
                }
                case ' ', '\n', '\t', '\r', '\f' -> advance();
                default -> {
                    return false;
                }
            }
        }
    }

    @Override
    public byte chomp() {
        index = 0;
        while (true) {
            switch (next1) {
                case 0 -> {
                    return 0;
                }
                case ' ', '\n', '\t', '\r', '\f' -> advance();
                default -> {
                    add(next1);
                    byte returned = next1;
                    advance();
                    index = 0;
                    return returned;
                }
            }
        }
    }

    @Override
    public LineSegment lexeme() {
        return LineSegments.of(currentLexeme, 0, index);
    }

    private void save() {
        add(next1);
        advance();
    }

    private void advance() {
        next1 = next2;
        next2 = nextChar();
    }

    private byte nextChar() {
        return (byte) this.nextChar.getAsInt();
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

    private boolean digital(byte peek) {
        return peek == '.' || isDigit(peek);
    }

    private void fail(String msg) {
        throw new ReadException(msg, "`" + lexeme().asString() + "`");
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
