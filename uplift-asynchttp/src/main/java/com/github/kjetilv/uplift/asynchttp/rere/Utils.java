package com.github.kjetilv.uplift.asynchttp.rere;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;

final class Utils {

    private Utils() {
    }

    static String string(MemorySegment memorySegment, int offset, int length) {
        return new String(
            memorySegment.asSlice(offset, length).toArray(ValueLayout.JAVA_BYTE),
            StandardCharsets.UTF_8
        );
    }
}
