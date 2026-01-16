package com.github.kjetilv.uplift.asynchttp.rere;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

import static com.github.kjetilv.uplift.asynchttp.rere.Utils.string;

public record RequestHeader(
    MemorySegment segment,
    Supplier<MemorySegment> downcasedName,
    int offset,
    int separatorOffset,
    int length
) {

    public RequestHeader(MemorySegment segment, int offset, int separatorOffset, int length) {
        var supplier = StableValue.supplier(() -> downcase(segment, offset, separatorOffset));
        this(segment, supplier, offset, separatorOffset, length);
    }

    public boolean isContentLength() {
        return nameLength() == CONTENT_LENGTH.byteSize() &&
               CONTENT_LENGTH.mismatch(this.downcasedName().get()) == -1;
    }

    public String name() {
        return string(segment, offset, nameLength());
    }

    public String value() {
        return string(segment, valueOffset(), valueLength());
    }

    private int nameLength() {
        return separatorOffset - offset;
    }

    private int valueOffset() {
        return separatorOffset + 2;
    }

    private int valueLength() {
        return length - (valueOffset() - offset);
    }

    private int valueLength(int valueOffset) {
        return length - (valueOffset - offset);
    }

    private static final MemorySegment CONTENT_LENGTH = MemorySegment.ofArray("content-length".getBytes());

    private static final char A = 'A';

    private static final char Z = 'Z';

    private static MemorySegment downcase(MemorySegment segment, int offset, int separatorOffset) {
        var length = separatorOffset - offset;
        var downcased = ByteBuffer.allocateDirect(length);
        var input = segment.asSlice(offset, length).asByteBuffer();
        downcased.put(input);
        for (int i = 0; i < length; i++) {
            var c = downcased.get(i);
            if (A <= c && c <= Z) {
                downcased.put(i, (byte) (c + 32));
            }
        }
        downcased.flip();
        return MemorySegment.ofBuffer(downcased);
    }

    @Override
    public String toString() {
        return name() + ": " + value();
    }
}
