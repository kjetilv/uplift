package com.github.kjetilv.uplift.json;

import java.io.InputStream;
import java.io.Reader;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

import com.github.kjetilv.uplift.json.events.EventHandler;

public final class Events {

    public static Callbacks parse(Callbacks callbacks, String source) {
        return EventHandler.parse(callbacks, source);
    }

    public static Callbacks parse(Callbacks callbacks, InputStream source) {
        return EventHandler.parse(callbacks, source);
    }

    public static Callbacks parse(Callbacks callbacks, Reader source) {
        return EventHandler.parse(callbacks, source);
    }

    public static Callbacks parse(Callbacks callbacks, MemorySegment memorySegment) {
        return parse(
            callbacks,
            Objects.requireNonNull(memorySegment, "memorySegment"),
            0L,
            memorySegment.byteSize()
        );
    }

    public static Callbacks parse(Callbacks callbacks, MemorySegment memorySegment, long startIndex, long endIndex) {
        return EventHandler.parse(callbacks, memorySegment, startIndex, endIndex);
    }

    private Events() {
    }
}
