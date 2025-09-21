package com.github.kjetilv.uplift.json.bytes;

import module java.base;
import module uplift.json;
import module uplift.util;

import static java.lang.Character.isDigit;

public abstract class AbstractIntsBytesSource implements BytesSource {

    private byte next1;

    private byte next2;

    private byte[] currentLexeme = new byte[1024];

    private int index;

    @Override
    public Bytes spoolField() {
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
                next2 = nextByte();
            }
        }
    }

    @Override
    public Bytes spoolString() {
        index = 0;
        boolean quo = false;
        while (true) {
            if (next1 >> 5 == 0) {
                if (quo) {
                    save();
                    quo = false;
                } else {
                    fail("Unescaped control char: " + (int) next1);
                }
                continue;
            }
            switch (next1) {
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
    public Bytes spoolNumber() {
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
    public void skip(char c1, char c2, char c3, char c4) {
        if (next1 != c1 || next2 != c2) {
            throw new IllegalStateException(
                "Expected '" + c1 + "'/'" + c2 + "' " +
                "but got '" + (char) next1 + "'/'" + (char) next2 + "'"
            );
        }
        byte next3 = nextByte();
        byte next4 = nextByte();
        if (next3 != c3 || next4 != c4) {
            throw new IllegalStateException(
                "Expected '" + c3 + "'/'" + c4 + "' " +
                "but got '" + (char) next2 + "'/'" + (char) next3 + "'"
            );
        }
        initialize();
    }

    @Override
    public void skip(char c1, char c2, char c3) {
        if (next1 != c1 || next2 != c2) {
            throw new IllegalStateException(
                "Expected '" + c1 + "'/'" + c2 + "' " +
                "but got '" + (char) next1 + "'/'" + (char) next2 + "'"
            );
        }
        byte next3 = nextByte();
        if (next3 != c3) {
            throw new IllegalStateException(
                "Expected '" + c3 + "' but got '" + (char) next2 + "'"
            );
        }
        initialize();
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
    public int chomp() {
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
    public Bytes lexeme() {
        return new Bytes(currentLexeme, 0, index);
    }

    protected final void initialize() {
        this.next1 = nextByte();
        this.next2 = next1 > 0 ? nextByte() : 0;
    }

    protected abstract byte nextByte();

    private void save() {
        add(next1);
        advance();
    }

    private void advance() {
        next1 = next2;
        next2 = nextByte();
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
        return isDigit(peek) || peek == '.';
    }

    private void fail(String msg) throws ReadException {
        throw new ReadException(msg, "`" + lexeme().string() + "`");
    }

    private static String print(int c) {
        return switch (c) {
            case 10 -> "\\n"; // LN
            case 13 -> "\\r"; // CR
            case 9 -> "\\t"; // tab
            case 8 -> "\\b"; // backspace
            default -> Character.toString(c);
        };
    }

    @Override
    public String toString() {
        int tail = Math.min(10, index); // LN
        int printOffset = index - tail;
        return getClass().getSimpleName() + "[" +
               "<" + new String(currentLexeme, printOffset, tail) + ">" +
               "<" + print(next1) + print(next2) + ">" +
               "]";
    }
}
