package com.github.kjetilv.uplift.json.bytes;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.MemorySegments;
import com.github.kjetilv.flopp.kernel.util.Bits;
import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.io.ReadException;

import java.lang.foreign.MemorySegment;
import java.util.function.LongSupplier;

import static java.lang.Character.isDigit;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class LineSegmentBytesSource implements BytesSource, LineSegment {

    private long pos;

    private byte current;

    private final LineSegment data;

    private final long startOffset;

    private long startIndex;

    private long endIndex;

    private final long limit;

    private final MemorySegment memorySegment;

    private final LongSupplier nextLong;

    private long currentLong;

    private long currentLongLimit;

    public LineSegmentBytesSource(LineSegment data) {
        this.data = data;
        this.startOffset = data.startIndex();
        this.limit = data.length();
        this.memorySegment = data.memorySegment();
        this.nextLong = data.longSupplier();
    }

    @Override
    public byte chomp() {
        long index = pos;
        byte b;
        while (index < limit && (b = bite(index)) != 0) {
            index++;
            if (!Character.isWhitespace(b)) {
                pos = index;
                return current = b;
            }
        }
        return 0;
    }

    @Override
    public LineSegment spoolField() {
        startIndex = pos;
        long index = pos;
        byte b;
        while ((b = bite(index)) != '"') {
            if (b >> 5 == 0) {
                fail("Unescaped control char: " + (char) current);
            }
            index++;
        }
        pos = index + 1;
        endIndex = index;
        return data;
    }

    @Override
    public LineSegment spoolString() {
        startIndex = pos;
        long index = pos;
        boolean quo = false;
        while (true) {
            byte b = bite(index);
            switch (b) {
                case '"' -> {
                    if (quo) {
                        quo = false;
                    } else {
                        pos = index + 1;
                        endIndex = index;
                        return this;
                    }
                }
                case '\\' -> quo = !quo;
                default -> quo = false;
            }
            index++;
        }
    }

    @Override
    public LineSegment spoolNumber() {
        startIndex = pos - 1;
        long index = pos;
        byte b;
        while (isDigit(b = bite(index))) {
            index++;
        }
        if (b == '.') {
            while (isDigit(bite(pos))) {
                index++;
            }
        }
        pos = index;
        endIndex = index;
        return this;
    }

    @Override
    public void skip5(byte c0, byte c1, byte c2, byte c3) {
        int value = data.unalignedIntAt(pos);
        int found = c0 + (c1 << 8) + (c2 << 16) + (c3 << 24);
        assert value == found : Bits.hxD(value) + " !=" + Bits.hxD(found);
        pos += 4;
    }

    @Override
    public void skip4(byte c0, byte c1, byte c2) {
        int value = data.unalignedIntAt(pos) & 0xFFFFFF;
        int found = c0 + (c1 << 8) + (c2 << 16);
        assert value == found : Bits.hxD(value) + " != " + Bits.hxD(found);
        pos += 3;
    }

    @Override
    public boolean done() {
        long index = pos;
        byte b;
        while (index < limit && (b = bite(index)) != 0) {
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
        return startOffset + startIndex;
    }

    @Override
    public long endIndex() {
        return startOffset + endIndex;
    }

    @Override
    public MemorySegment memorySegment() {
        return memorySegment;
    }

    @Override
    public byte byteAt(long i) {
        return memorySegment.get(JAVA_BYTE, startOffset + startIndex + i);
    }

    private byte bite(long index) {
        if (index >= currentLongLimit) {
            currentLong = nextLong.getAsLong();
            currentLongLimit += MemorySegments.ALIGNMENT;
        }
        long pos = index % MemorySegments.ALIGNMENT;
        return Bits.getByte(currentLong, pos);
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
