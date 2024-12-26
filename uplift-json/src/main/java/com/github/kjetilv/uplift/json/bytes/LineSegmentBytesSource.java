package com.github.kjetilv.uplift.json.bytes;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.util.Bits;
import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.io.ReadException;

import java.lang.foreign.MemorySegment;

import static java.lang.Character.isDigit;

public final class LineSegmentBytesSource implements BytesSource, LineSegment {

    private long pos = -1;

    private byte current;

    private final LineSegment data;

    private long startIndex;

    private long endIndex;

    public LineSegmentBytesSource(LineSegment data) {
        this.data = data;
        advance();
    }

    @Override
    public void spoolField() {
        startIndex = pos;
        while (true) {
            try {
                if (current >> 5 == 0) {
                    fail("Unescaped control char: " + (char) current);
                }
                if (current == '"') {
                    endIndex = pos;
                    return;
                }
            } finally {
                advance();
            }
        }
    }

    @Override
    public void spoolString() {
        startIndex = pos ;
        boolean quo = false;
        while (true) {
            try {
                byte c = current;
                if (c >> 5 == 0) {
                    if (quo) {
                        quo = false;
                    } else {
                        fail("Unescaped control char: " + (int) c);
                    }
                    continue;
                }
                switch (c) {
                    case '"' -> {
                        if (quo) {
                            quo = false;
                        } else {
                            endIndex = pos;
                            return;
                        }
                    }
                    case '\\' ->
                        quo = !quo;
                    default ->
                        quo = false;
                }
            } finally {
                advance();
            }
        }
    }

    @Override
    public void spoolNumber() {
        startIndex = pos - 1;
        while (digital(current)) {
            advance();
        }
        // Look for a fractional part.
        if (current == '.' && isDigit(advance())) {
            // Consume the "."
            do {
                advance();
            } while (isDigit(current));
        }
        endIndex = pos;
    }

    @Override
    public void skip5(byte c0, byte c1, byte c2, byte c3) {
        assert current == c0;
        int value = data.unalignedShortAt(pos + 1);
        assert value == c1 + (c2 << 8) : Bits.hxD(value) + " !=" + Bits.hxD(c1 + (c2 << 8));
        byte byteValue = data.byteAt(pos + 3);
        assert byteValue == c3;
        advance(4);
    }

    @Override
    public void skip4(byte c0, byte c1, byte c2) {
        assert current == c0;
        int shortValue = data.unalignedShortAt(pos + 1);
        assert shortValue == c1 + (c2 << 8) : Bits.hxD(shortValue) + " != " + Bits.hxD(c1 + (c2 << 8));
        advance(3);
    }

    @Override
    public boolean done() {
        while (true) {
            if (current == 0) {
                return true;
            }
            if (!Character.isWhitespace(current)) {
                return false;
            }
            advance();
        }
    }

    @Override
    public byte chomp() {
        try {
            return current;
        } finally {
            advance();
        }
    }

    @Override
    public LineSegment lexeme() {
        return this;
    }

    @Override
    public void reset() {
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

    private byte advance() {
        pos++;
        return current = data.byteAt(pos);
    }

    private byte advance(int steps) {
        pos += steps;
        return current = data.byteAt(pos);
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
        return getClass().getSimpleName() + "[" + data + " " +
               "<" + asString() + ">" +
               "<" + print(current) + print(data.byteAt(pos + 1)) + ">]";
    }
}
