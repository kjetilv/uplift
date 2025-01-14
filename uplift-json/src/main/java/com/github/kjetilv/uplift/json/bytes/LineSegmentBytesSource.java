package com.github.kjetilv.uplift.json.bytes;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.Bits;
import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.io.ReadException;

import java.lang.foreign.MemorySegment;
import java.util.function.LongSupplier;

import static com.github.kjetilv.flopp.kernel.MemorySegments.ALIGNMENT;
import static java.lang.Character.isDigit;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class LineSegmentBytesSource implements BytesSource, LineSegment {

    private final LineSegment data;

    private final MemorySegment memorySegment;

    private final long startOffset;

    private final long limit;

    private final LongSupplier nextLong;

    private final Bits.Finder quotesFinder = Bits.finder('"', true);

    private int currentByte;

    private long currentLong;

    private long currentLongLimit;

    private long pos;

    private long startIndex;

    private long endIndex;

    public LineSegmentBytesSource(LineSegment data) {
        this.data = data;
        this.startOffset = data.startIndex();
        this.limit = data.length();
        this.memorySegment = data.memorySegment();
        this.nextLong = data.longSupplier();
    }

    @Override
    public int chomp() {
        long index = pos;
        int b;
        while (index < limit && (b = biteAt(index)) != 0) {
            index++;
            if (!Character.isWhitespace((byte) b)) {
                pos = index;
                return currentByte = b;
            }
        }

        return 0;
    }

    @Override
    public LineSegment spoolField() {
        startIndex = pos;

        long offset = startIndex % ALIGNMENT;
        endIndex = startIndex - offset + ALIGNMENT;

        if (offset > 0) {
            currentLong = Bits.clearLow(currentLong, (int) offset);
        } else {
            advance();
        }

        int nextQ = quotesFinder.next(currentLong);
        while (true) {
            if (nextQ == ALIGNMENT) {
                advance();
                endIndex += ALIGNMENT;
                nextQ = quotesFinder.next(currentLong);
            } else {
                endIndex -= (ALIGNMENT - nextQ);
                pos = endIndex + 1;
                return this;
            }
        }
    }

    @Override
    public LineSegment spoolString() {
        startIndex = pos;
        long index = pos;
        boolean quo = false;
        while (true) {
            int b = biteAt(index);
            if (b >> 5 == 0 && !quo) {
                return fail("Unescaped control char: " + (char) currentByte);
            }
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
        int b;
        while (isDigit(b = biteAt(index))) {
            index++;
        }
        if (b == '.') {
            index++;
            while (isDigit(biteAt(index))) {
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
        int b;
        while (index < limit && (b = biteAt(index)) != 0) {
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

    private void advance() {
        currentLong = nextLong.getAsLong();
        currentLongLimit += ALIGNMENT;
    }

    private <T> T fail(String msg) {
        throw new ReadException(msg, "`" + lexeme().asString() + "`");
    }

    private int biteAt(long index) {
        while (index >= currentLongLimit) {
            currentLong = nextLong.getAsLong();
            currentLongLimit += ALIGNMENT;
        }
        return Bits.getByte(currentLong, index % ALIGNMENT);
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
               "<" + print(currentByte) + Bits.toString(data.unalignedIntAt(pos - 2), 4, UTF_8) + ">]";
    }
}
