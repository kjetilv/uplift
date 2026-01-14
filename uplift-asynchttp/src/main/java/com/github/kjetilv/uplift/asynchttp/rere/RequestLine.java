package com.github.kjetilv.uplift.asynchttp.rere;

import java.lang.foreign.MemorySegment;

import static com.github.kjetilv.uplift.asynchttp.rere.Utils.string;

public record RequestLine(MemorySegment memorySegment, int urlIndex, int versionIndex, int lineBreak) {

    public String method() {
        return string(memorySegment, 0, urlIndex - 1);
    }

    public String url() {
        return string(memorySegment, urlIndex, versionIndex - urlIndex - 1);
    }

    public String version() {
        return string(memorySegment, versionIndex, lineBreak - versionIndex - 1);
    }

    @Override
    public String toString() {
        return method() + " " + url() + " " + version();
    }
}
