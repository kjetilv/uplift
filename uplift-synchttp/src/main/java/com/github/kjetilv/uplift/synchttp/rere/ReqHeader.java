package com.github.kjetilv.uplift.synchttp.rere;

import module java.base;
import com.github.kjetilv.uplift.synchttp.Utils;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

public record ReqHeader(
    MemorySegment segment,
    int offset,
    int separatorOffset,
    int length
) {

    public boolean isContentLength() {
        return is(CONTENT_LENGTH);
    }

    /// @param lowercaseName Name in lower case
    /// @return True if this header has the name, ignoring case
    public boolean is(byte[] lowercaseName) {
        return is(MemorySegment.ofArray(lowercaseName));
    }

    /// @param lowercaseName Name in lower case
    /// @return True if this header has the name, ignoring case
    public boolean is(MemorySegment lowercaseName) {
        var length = nameLength();
        if (lowercaseName.byteSize() != length) {
            return false;
        }
        for (var i = 0; i < length; i++) {
            if (lowercase(segment.get(JAVA_BYTE, offset + i)) != lowercaseName.get(JAVA_BYTE, i)) {
                return false;
            }
        }
        return true;
    }

    /// @param name Name, in ASCII
    /// @return True if this header has the name, ignoring case
    public boolean is(String name) {
        var length = nameLength();
        if (name.length() != length) {
            return false;
        }
        for (var i = 0; i < length; i++) {
            var c = name.charAt(i);
            if (c >= 0x80 || lowercase(segment.get(JAVA_BYTE, offset + i)) != lowercase((byte) c)) {
                return false;
            }
        }
        return true;
    }

    public String name() {
        return Utils.string(segment, offset, nameLength());
    }

    /// @return The value, without spaces and tabs before and after it
    public String value() {
        var start = separatorOffset + 1;
        var end = offset + length;
        while (start < end && isWhitespace(segment.get(JAVA_BYTE, start))) {
            start++;
        }
        while (end > start && isWhitespace(segment.get(JAVA_BYTE, end - 1))) {
            end--;
        }
        return start == end ? "" : Utils.string(segment, start, end - start);
    }

    private int nameLength() {
        return separatorOffset - offset;
    }

    private static final MemorySegment CONTENT_LENGTH = MemorySegment.ofArray("content-length".getBytes());

    private static boolean isWhitespace(byte b) {
        return b == ' ' || b == '\t';
    }

    private static byte lowercase(byte b) {
        return b >= 'A' && b <= 'Z' ? (byte) (b + ('a' - 'A')) : b;
    }

    @Override
    public String toString() {
        try {
            return name() + ": " + value();
        } catch (Exception e) {
            return getClass().getSimpleName() + "[" + segment + "]";
        }
    }
}
