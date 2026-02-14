package com.github.kjetilv.uplift.synchttp.rere;

import com.github.kjetilv.uplift.synchttp.Utils;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

public record ReqHeader(
    MemorySegment segment,
    Supplier<MemorySegment> downcasedName,
    int offset,
    int separatorOffset,
    int length
) {

    public ReqHeader(MemorySegment segment, int offset, int separatorOffset, int length) {
        var supplier = StableValue.supplier(() -> downcase(segment, offset, separatorOffset));
        this(segment, supplier, offset, separatorOffset, length);
    }

    public boolean is(String header) {
        var bytes = header.toLowerCase(Locale.ROOT).getBytes(UTF_8);
        return nameLength() == bytes.length &&
               MemorySegment.ofArray(bytes).mismatch(this.downcasedName().get()) == -1;
    }

    public boolean isContentLength() {
        return nameLength() == CONTENT_LENGTH.byteSize() && CONTENT_LENGTH.mismatch(this.downcasedName().get()) == -1;
    }

    public String name() {
        return Utils.string(segment, offset, nameLength());
    }

    public String value() {
        return Utils.string(segment, valueOffset(), valueLength());
    }

    private int nameLength() {
        return separatorOffset - offset;
    }

    private int valueLength() {
        return length - (valueOffset() - offset);
    }

    private int valueOffset() {
        return separatorOffset + 2;
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
