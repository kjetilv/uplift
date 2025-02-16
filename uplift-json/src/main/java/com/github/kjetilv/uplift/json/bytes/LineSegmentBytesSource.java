package com.github.kjetilv.uplift.json.bytes;

import com.github.kjetilv.flopp.kernel.Bits;
import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.uplift.json.BytesSource;

import java.lang.foreign.MemorySegment;
import java.util.function.LongSupplier;

import static com.github.kjetilv.flopp.kernel.MemorySegments.ALIGNMENT;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class LineSegmentBytesSource implements BytesSource, LineSegment {

    private final LineSegment data;

    private final MemorySegment memorySegment;

    private final long startOffset;

    private final long limit;

    private final LongSupplier nextLong;

    private final Bits.Finder quotesFinder = Bits.finder('"', true);

    private final Bits.Finder revSolFinder = Bits.finder('\\', true);

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
        while (pos < limit) {
            try {
                switch (biteAt(pos)) {
                    case ' ', '\n', '\t', '\r', '\f' -> {
                    }
                    case int bite -> {
                        return currentByte = bite;
                    }
                }
            } finally {
                pos++;
            }
        }
        return 0;
    }

    @Override
    public LineSegment spoolField() {
        alignStartEnd();

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
        alignStartEnd();

        long lastQuote = -1L;
        boolean quoted = false;

        int nextQ = quotesFinder.next(currentLong);
        int nextR = revSolFinder.next(currentLong);

        while (true) {
            if (nextQ == nextR) { // No escape or quote in sight
                advance();
                endIndex += ALIGNMENT;
                nextQ = quotesFinder.next(currentLong);
                nextR = revSolFinder.next(currentLong);
                continue;
            }
            if (nextR < nextQ) { // Next is escape
                lastQuote = endIndex + nextR;
                quoted = true;
                nextR = revSolFinder.next();
                continue;
            }
            // Next is quote
            long position = endIndex + nextQ;
            if (quoted && lastQuote == position - 1) { // Escaped quote
                lastQuote = -1;
                nextQ = quotesFinder.next();
                continue;
            }
            endIndex -= (ALIGNMENT - nextQ);
            pos = endIndex + 1;
            return this;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public LineSegment spoolNumber() {
        startIndex = pos - 1;
        int b;
        while (isDigit(b = biteAt(pos))) {
            pos++;
        }
        if (b == '.') {
            while (isDigit(biteAt(pos++)));
        }
        endIndex = pos;
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

    private void alignStartEnd() {
        startIndex = pos;
        long offset = startIndex % ALIGNMENT;
        endIndex = startIndex - offset + ALIGNMENT;
        if (offset > 0) {
            currentLong = Bits.clearLow(currentLong, (int) offset);
        } else {
            advance();
        }
    }

    private void advance() {
        currentLong = nextLong.getAsLong();
        currentLongLimit += ALIGNMENT;
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

    private static boolean isDigit(int b) {
        return '0' <= b && b <= '9';
    }

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
