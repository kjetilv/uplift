package com.github.kjetilv.uplift.json.bytes;

import module java.base;
import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.io.ReadException;
import com.github.kjetilv.uplift.util.Bytes;

import static java.lang.Character.isDigit;

public abstract class AbstractIntsBytesSource implements BytesSource {

    private byte prev;

    private byte next1;

    private byte next2;

    private byte[] currentLexeme = new byte[1024];

    private int index;

    /// Number of bytes pulled from the source, including the two look-ahead bytes
    private long pulled;

    /// Reads the first two bytes. A UTF-8 byte order mark at the start is skipped.
    @SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
    public AbstractIntsBytesSource() {
        this.next1 = pull();
        this.next2 = next1 != 0 ? pull() : 0;
        if (next1 == BOM_1 && next2 == BOM_2) {
            if (pull() != BOM_3) {
                fail("Invalid byte order mark");
            }
            this.next1 = pull();
            this.next2 = next1 != 0 ? pull() : 0;
        }
    }

    @Override
    public Bytes spoolField() {
        return spoolQuoted();
    }

    @Override
    public Bytes spoolString() {
        return spoolQuoted();
    }

    /// @return Approximate byte offset of the next unread byte
    @Override
    public long position() {
        return Math.max(0, pulled - 2);
    }

    /// Collects the characters of a number. The grammar is checked by the caller.
    @Override
    public Bytes spoolNumber() {
        index++; // First digit is already in buffer
        while (isDigit(next1) ||
               next1 == '.' ||
               next1 == 'e' ||
               next1 == 'E' ||
               next1 == '-' && (prev == 'e' || prev == 'E') ||
               next1 == '+' && (prev == 'e' || prev == 'E')) {
            save();
        }
        return lexeme();
    }

    @Override
    public void skip(char c1, char c2, char c3, char c4) {
        if (next1 != c1 || next2 != c2) {
            failLiteral("" + c1 + c2 + c3 + c4, "" + (char) next1 + (char) next2);
        }
        var next3 = pull();
        var next4 = pull();
        if (next3 != c3 || next4 != c4) {
            failLiteral("" + c1 + c2 + c3 + c4, "" + c1 + c2 + (char) next3 + (char) next4);
        }
        this.next1 = pull();
        this.next2 = next1 != 0 ? pull() : 0;
    }

    @Override
    public void skip(char c1, char c2, char c3) {
        if (next1 != c1 || next2 != c2) {
            failLiteral("" + c1 + c2 + c3, "" + (char) next1 + (char) next2);
        }
        var next3 = pull();
        if (next3 != c3) {
            failLiteral("" + c1 + c2 + c3, "" + c1 + c2 + (char) next3);
        }
        this.next1 = pull();
        this.next2 = next1 != 0 ? pull() : 0;
    }

    @Override
    public boolean done() {
        while (true) {
            switch (next1) {
                case ' ', '\n', '\t', '\r', '\f' -> advance();
                default -> {
                    return next1 == 0;
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
                    var returned = next1;
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

    /// @return Next byte, 0 when done
    protected abstract byte nextByte();

    /// Reads a string after the opening quote, up to and including the closing quote. Escapes are decoded:
    /// the lexeme holds the UTF-8 bytes of the decoded string.
    private Bytes spoolQuoted() {
        index = 0;
        while (true) {
            var b = next1;
            if (b == '"') {
                advance();
                return lexeme();
            }
            if (b == '\\') {
                advance();
                unescape();
            } else if (b >= 0 && b < 0x20) {
                fail(b == 0 ? "Unterminated string" : "Unescaped control character " + b);
            } else {
                save();
            }
        }
    }

    /// Decodes the escape sequence at `next1`, after the backslash. A malformed unicode escape is an error.
    private void unescape() {
        switch (next1) {
            case '"', '\\', '/' -> save();
            case 'b' -> addEscaped('\b');
            case 'f' -> addEscaped('\f');
            case 'n' -> addEscaped('\n');
            case 'r' -> addEscaped('\r');
            case 't' -> addEscaped('\t');
            case 'u' -> {
                advance();
                addUtf8(unicodeEscape());
            }
            case 0 -> fail("Unterminated string");
            // Lenient, as before: an unknown escaped character, also a raw control character, is kept as it is
            default -> save();
        }
    }

    /// Decodes the hex digits of a `\\u` escape, and a following low surrogate escape if the first is a high
    /// surrogate. A lone surrogate becomes U+FFFD.
    ///
    /// @return Code point
    private int unicodeEscape() {
        var unit = hex4();
        if (Character.isHighSurrogate((char) unit) && next1 == '\\' && next2 == 'u') {
            advance();
            advance();
            var low = hex4();
            if (Character.isLowSurrogate((char) low)) {
                return Character.toCodePoint((char) unit, (char) low);
            }
            addUtf8(REPLACEMENT);
            return Character.isSurrogate((char) low) ? REPLACEMENT : low;
        }
        return Character.isSurrogate((char) unit) ? REPLACEMENT : unit;
    }

    private int hex4() {
        var value = 0;
        for (var i = 0; i < 4; i++) {
            var digit = Character.digit(next1, 16);
            if (digit < 0) {
                fail("Invalid \\u escape");
            }
            value = value << 4 | digit;
            advance();
        }
        return value;
    }

    private void addEscaped(char c) {
        add((byte) c);
        advance();
    }

    private void addUtf8(int codePoint) {
        if (codePoint < 0x80) {
            add((byte) codePoint);
        } else if (codePoint < 0x800) {
            add((byte) (0xC0 | codePoint >> 6));
            add((byte) (0x80 | codePoint & 0x3F));
        } else if (codePoint < 0x10000) {
            add((byte) (0xE0 | codePoint >> 12));
            add((byte) (0x80 | codePoint >> 6 & 0x3F));
            add((byte) (0x80 | codePoint & 0x3F));
        } else {
            add((byte) (0xF0 | codePoint >> 18));
            add((byte) (0x80 | codePoint >> 12 & 0x3F));
            add((byte) (0x80 | codePoint >> 6 & 0x3F));
            add((byte) (0x80 | codePoint & 0x3F));
        }
    }

    private byte pull() {
        pulled++;
        return nextByte();
    }

    private void save() {
        add(next1);
        advance();
    }

    private void advance() {
        prev = next1;
        next1 = next2;
        next2 = pull();
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
        var biggerLexeme = new byte[index * 2];
        System.arraycopy(currentLexeme, 0, biggerLexeme, 0, index);
        currentLexeme = biggerLexeme;
    }

    private void fail(String msg) {
        throw new ReadException(msg, "<" + lexeme().string() + ">", position());
    }

    private void failLiteral(String expected, String actual) {
        throw new ReadException("Expected `" + expected + "`", "<" + actual + ">", position());
    }

    private static final int REPLACEMENT = 0xFFFD;

    private static final byte BOM_1 = (byte) 0xEF;

    private static final byte BOM_2 = (byte) 0xBB;

    private static final byte BOM_3 = (byte) 0xBF;

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
        var tail = Math.min(10, index); // LN
        var printOffset = index - tail;
        return getClass().getSimpleName() + "[" +
               "<" + new String(currentLexeme, printOffset, tail) + ">" +
               "<" + print(next1) + print(next2) + ">" +
               "]";
    }
}
