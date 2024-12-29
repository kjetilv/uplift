package com.github.kjetilv.uplift.json.bytes;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.util.Bits;
import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.io.ReadException;

import java.lang.foreign.MemorySegment;

import static java.lang.Character.isDigit;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class LineSegmentBytesSource implements BytesSource, LineSegment {

    private long pos;

    private byte current;

    private final LineSegment data;

    private long startIndex;

    private long endIndex;

    private final long limit;

    public LineSegmentBytesSource(LineSegment data) {
        this(data, false);
    }

    public LineSegmentBytesSource(LineSegment data, boolean aligned) {
        this.data = data;
        this.limit = data.length();
    }

    @Override
    public byte chomp() {
        long index = pos;
        byte b;
        while (index < limit && (b = data.byteAt(index)) != 0) {
            index++;
            if (!Character.isWhitespace(b)) {
                pos = index;
                return current = b;
            }
        }
        return 0;
    }

    @Override
    public void spoolField() {
        startIndex = pos;
        long index = pos;
        byte b;
        while ((b = data.byteAt(index)) != '"') {
            if (b >> 5 == 0) {
                fail("Unescaped control char: " + (char) current);
            }
            index++;
        }
        pos = index + 1;
        endIndex = index;
    }

    @Override
    public void spoolString() {
        startIndex = pos;
        long index = pos;
        boolean quo = false;
        while (true) {
            byte b = data.byteAt(index);
            if (b >> 5 == 0) {
                if (quo) {
                    quo = false;
                } else {
                    fail("Unescaped control char: " + (int) b);
                }
                continue;
            }
            switch (b) {
                case '"' -> {
                    if (quo) {
                        quo = false;
                    } else {
                        pos = index + 1;
                        endIndex = index;
                        return;
                    }
                }
                case '\\' -> quo = !quo;
                default -> quo = false;
            }
            index++;
        }
    }

    @Override
    public void spoolNumber() {
        startIndex = pos - 1;
        long index = pos;
        byte b;
        while (isDigit(b = data.byteAt(index))) {
            index++;
        }
        if (b == '.') {
            while (isDigit(b = data.byteAt(pos))) {
                index++;
            }
        }
        pos = index;
        endIndex = index;
    }

    @Override
    public void skip5(byte c0, byte c1, byte c2, byte c3) {
        int value = data.unalignedIntAt(pos);
        assert value == c0 + (c1 << 8) + (c2 << 16) + (c3 << 24)
            : Bits.hxD(value) + " !=" + Bits.hxD(c0 + (c1 << 8) + (c2 << 16) + (c3 << 24));
        pos += 4;
    }

    @Override
    public void skip4(byte c0, byte c1, byte c2) {
        int shortValue = data.unalignedIntAt(pos) & 0xFFFFFF;
        assert shortValue == c0 + (c1 << 8) + (c2 << 16)
            : Bits.hxD(shortValue) + " != " + Bits.hxD(c0 + (c1 << 8) + (c2 << 16));
        pos += 3;
    }

    @Override
    public boolean done() {
        long index = pos;
        byte b;
        while (index < limit && (b = data.byteAt(index)) != 0) {
            index++;
            if (!Character.isWhitespace(b)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public LineSegment lexeme() {
        return this;
    }

    @Override
    public long startIndex() {
        return startIndex;
    }

    @Override
    public long endIndex() {
        return endIndex;
    }

    @Override
    public MemorySegment memorySegment() {
        return data.memorySegment();
    }

    private void advance() {
        while (true) {
            pos++;
            byte b = data.byteAt(pos);
            if (b == 0) {
                return;
            }
            if (!Character.isWhitespace(b)) {
                current = b;
                return;
            }
        }
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
        return getClass().getSimpleName() + "[" + data + " " +
               "<" + asString() + ">" +
               "<" + print(current) + Bits.toString(data.unalignedIntAt(pos - 2), 4, UTF_8) + ">]";
    }
}
