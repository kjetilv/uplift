package com.github.kjetilv.uplift.asynchttp.rere;

import java.lang.foreign.MemorySegment;

import static com.github.kjetilv.uplift.asynchttp.rere.Utils.string;

public record RequestHeader(
    MemorySegment memorySegment,
    int offset,
    int separatorOffset,
    int length
) {

    public boolean isContentLength() {
        return separatorOffset == CONTENT_LENGTH_LENGTH &&
               name().equalsIgnoreCase(CONTENT_LENGTH);
    }

    public String name() {
        return string(memorySegment, offset, separatorOffset - offset);
    }

    public String value() {
        var valueOffset = separatorOffset + 2;
        return string(memorySegment, valueOffset, length - (valueOffset - offset));
    }

    private static final String CONTENT_LENGTH = "Content-Length";

    private static final int CONTENT_LENGTH_LENGTH = CONTENT_LENGTH.length();

    @Override
    public String toString() {
        return name() + ": " + value();
    }
}
