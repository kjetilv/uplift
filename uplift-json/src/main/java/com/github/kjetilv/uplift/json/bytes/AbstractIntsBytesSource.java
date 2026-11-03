package com.github.kjetilv.uplift.json.bytes;

import module java.base;
import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.io.ReadException;
import com.github.kjetilv.uplift.util.Bytes;

/// Lexer over a window of bytes.
///
/// Sources that hold all input in a byte array pass it to [#AbstractIntsBytesSource(byte\[\], int, int)], and
/// are scanned in place. Streaming sources use [#AbstractIntsBytesSource()] and implement [#read(byte\[\], int, int)];
/// the window is then a buffer, refilled as it is consumed.
///
/// Lexemes are returned as views into the window where possible: a string without escapes, or a number,
/// that lies within the window. Otherwise they are collected in a separate buffer. A lexeme is valid until the
/// next call to this source.
public abstract class AbstractIntsBytesSource implements BytesSource {

    private final byte[] buf;

    private final boolean streaming;

    private boolean exhausted;

    private int pos;

    private int limit;

    /// Offset in the input of `buf[0]`
    private long base;

    private byte[] collected = NO_BYTES;

    private int index;

    private Bytes lexeme = EMPTY;

    /// Streaming source: input is supplied by [#read(byte\[\], int, int)]
    @SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
    protected AbstractIntsBytesSource() {
        this.buf = new byte[BUFFER_SIZE];
        this.streaming = true;
        skipByteOrderMark();
    }

    /// Source with all input in an array, scanned in place
    ///
    /// @param bytes  Input
    /// @param offset Start of input
    /// @param length Length of input
    protected AbstractIntsBytesSource(byte[] bytes, int offset, int length) {
        Objects.checkFromIndexSize(offset, length, bytes.length);
        this.buf = bytes;
        this.streaming = false;
        this.pos = offset;
        this.limit = offset + length;
        this.base = -offset;
        skipByteOrderMark();
    }

    @Override
    public int chomp() {
        lexeme = EMPTY;
        while (true) {
            var c = peek();
            switch (c) {
                case 0 -> {
                    return 0;
                }
                case ' ', '\n', '\t', '\r', '\f' -> pos++;
                default -> {
                    pos++;
                    return c;
                }
            }
        }
    }

    @Override
    public boolean done() {
        while (true) {
            switch (peek()) {
                case ' ', '\n', '\t', '\r', '\f' -> pos++;
                case 0 -> {
                    return true;
                }
                default -> {
                    return false;
                }
            }
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

    /// Collects the characters of a number, starting with the character returned by the last [#chomp()].
    /// The grammar is checked by the caller.
    @Override
    public Bytes spoolNumber() {
        index = 0;
        var start = pos - 1;
        var prev = buf[start];
        while (true) {
            var i = pos;
            while (i < limit && isNumberPart(buf[i], prev)) {
                prev = buf[i];
                i++;
            }
            pos = i;
            if (i < limit || !streaming) {
                return lexeme = index == 0
                    ? new Bytes(buf, start, i - start)
                    : collect(start, i);
            }
            collect(start, i);
            if (!fill()) {
                return lexeme;
            }
            start = pos;
        }
    }

    @Override
    public void skip(char c1, char c2, char c3, char c4) {
        expect(c1, c2, c3, c4);
    }

    @Override
    public void skip(char c1, char c2, char c3) {
        expect(c1, c2, c3);
    }

    @Override
    public Bytes lexeme() {
        return lexeme;
    }

    /// @return Approximate byte offset of the next unread byte
    @Override
    public long position() {
        return base + pos;
    }

    /// Reads more input. Called by streaming sources only, when the window is consumed.
    ///
    /// @param dst    Buffer
    /// @param offset Start in buffer
    /// @param length Maximum number of bytes
    /// @return Number of bytes read, or -1 at the end of input
    protected int read(byte[] dst, int offset, int length) {
        return -1;
    }

    /// Reads a string after the opening quote, up to and including the closing quote. Escapes are decoded:
    /// the lexeme holds the UTF-8 bytes of the decoded string.
    private Bytes spoolQuoted() {
        index = 0;
        var start = pos;
        while (true) {
            var i = pos;
            while (i < limit) {
                var b = buf[i];
                if (b == '"' || b == '\\' || b >= 0 && b < 0x20) {
                    break;
                }
                i++;
            }
            if (i == limit) {
                append(start, i);
                pos = i;
                if (!fill()) {
                    fail("Unterminated string");
                }
                start = pos;
                continue;
            }
            var b = buf[i];
            if (b == '"') {
                pos = i + 1;
                return lexeme = index == 0
                    ? new Bytes(buf, start, i - start)
                    : collect(start, i);
            }
            if (b == '\\') {
                append(start, i);
                pos = i + 1;
                unescape();
                start = pos;
            } else {
                append(start, i);
                pos = i;
                fail("Unescaped control character " + b);
            }
        }
    }

    /// Decodes the escape sequence after a backslash. A malformed unicode escape is an error.
    private void unescape() {
        var c = next();
        switch (c) {
            case 'b' -> add((byte) '\b');
            case 'f' -> add((byte) '\f');
            case 'n' -> add((byte) '\n');
            case 'r' -> add((byte) '\r');
            case 't' -> add((byte) '\t');
            case 'u' -> unicodeEscape();
            case 0 -> fail("Unterminated string");
            // Lenient, as before: an unknown escaped character, also a raw control character, is kept as it is
            default -> add(c);
        }
    }

    /// Decodes the hex digits of a unicode escape, and a following low surrogate escape if the first is a high
    /// surrogate. A lone surrogate becomes U+FFFD.
    private void unicodeEscape() {
        var unit = (char) hex4();
        if (!Character.isHighSurrogate(unit)) {
            addUtf8(Character.isSurrogate(unit) ? REPLACEMENT : unit);
            return;
        }
        if (peek() != '\\') {
            addUtf8(REPLACEMENT);
            return;
        }
        pos++;
        if (peek() != 'u') {
            addUtf8(REPLACEMENT);
            unescape();
            return;
        }
        pos++;
        var low = (char) hex4();
        if (Character.isLowSurrogate(low)) {
            addUtf8(Character.toCodePoint(unit, low));
        } else {
            addUtf8(REPLACEMENT);
            addUtf8(Character.isSurrogate(low) ? REPLACEMENT : low);
        }
    }

    private int hex4() {
        var value = 0;
        for (var i = 0; i < 4; i++) {
            var digit = Character.digit(next(), 16);
            if (digit < 0) {
                fail("Invalid unicode escape");
            }
            value = value << 4 | digit;
        }
        return value;
    }

    private void expect(char... chars) {
        for (var c : chars) {
            var b = next();
            if (b != c) {
                throw new ReadException(
                    "Expected `" + new String(chars) + "`",
                    "<" + (char) b + ">",
                    position()
                );
            }
        }
    }

    private void skipByteOrderMark() {
        if (peek() == BOM_1) {
            pos++;
            if (next() != BOM_2 || next() != BOM_3) {
                fail("Invalid byte order mark");
            }
        }
    }

    /// @return Next byte without consuming it, or 0 at the end of input
    private byte peek() {
        return pos < limit || fill() ? buf[pos] : 0;
    }

    /// @return Next byte, or 0 at the end of input
    private byte next() {
        return pos < limit || fill() ? buf[pos++] : 0;
    }

    /// Replaces a consumed window with new input.
    ///
    /// @return true iff there is input
    private boolean fill() {
        if (!streaming || exhausted) {
            return false;
        }
        base += limit;
        pos = 0;
        limit = 0;
        while (true) {
            var read = read(buf, 0, buf.length);
            if (read < 0) {
                exhausted = true;
                return false;
            }
            if (read > 0) {
                limit = read;
                return true;
            }
        }
    }

    /// Appends a range of the window to the collected bytes.
    private void append(int from, int to) {
        var length = to - from;
        ensureCapacity(length);
        System.arraycopy(buf, from, collected, index, length);
        index += length;
    }

    /// Appends a range of the window to the collected bytes.
    ///
    /// @return The collected bytes, as the lexeme
    private Bytes collect(int from, int to) {
        append(from, to);
        return lexeme = new Bytes(collected, 0, index);
    }

    private void add(byte b) {
        ensureCapacity(1);
        collected[index++] = b;
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

    private void ensureCapacity(int extra) {
        var needed = index + extra;
        if (needed > collected.length) {
            collected = Arrays.copyOf(collected, Math.max(needed, Math.max(MIN_COLLECTED, collected.length * 2)));
        }
    }

    private void fail(String msg) {
        throw new ReadException(msg, "<" + new String(collected, 0, index, StandardCharsets.UTF_8) + ">", position());
    }

    private static final int BUFFER_SIZE = 8192;

    private static final int MIN_COLLECTED = 64;

    private static final byte[] NO_BYTES = new byte[0];

    private static final Bytes EMPTY = new Bytes(NO_BYTES, 0, 0);

    private static final int REPLACEMENT = 0xFFFD;

    private static final byte BOM_1 = (byte) 0xEF;

    private static final byte BOM_2 = (byte) 0xBB;

    private static final byte BOM_3 = (byte) 0xBF;

    /// Number characters: digits, `.`, `e`, `E`, and `+` or `-` right after `e` or `E`. A `-` elsewhere ends the
    /// number, so that `5-5` is read as two numbers.
    private static boolean isNumberPart(byte b, byte prev) {
        return b >= '0' && b <= '9' ||
               b == '.' ||
               b == 'e' ||
               b == 'E' ||
               (b == '-' || b == '+') && (prev == 'e' || prev == 'E');
    }

    @Override
    public String toString() {
        var from = Math.max(0, pos - 10);
        var to = Math.min(limit, pos + 2);
        return getClass().getSimpleName() + "[" +
               "<" + new String(buf, from, Math.max(0, pos - from), StandardCharsets.ISO_8859_1) + ">" +
               "<" + new String(buf, pos, Math.max(0, to - pos), StandardCharsets.ISO_8859_1) + ">" +
               "]";
    }
}
