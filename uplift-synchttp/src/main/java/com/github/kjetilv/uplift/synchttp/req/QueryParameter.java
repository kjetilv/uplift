package com.github.kjetilv.uplift.synchttp.req;

import com.github.kjetilv.uplift.synchttp.util.Utils;

import java.lang.foreign.MemorySegment;

public record QueryParameter(MemorySegment segment, long offset, long eqOffset, long length) {

    public String name() {
        return Utils.string(segment, offset, nameLength());
    }

    @Override
    public String toString() {
        return name() + "=" + value();
    }

    public String value() {
        return Utils.string(segment, valueOffset(), length);
    }

    public long nameLength() {
        return eqOffset - offset;
    }

    public boolean hasName(String name) {
        return nameLength() == name.length() && Utils.isEqual(name, segment, offset);
    }

    private long valueOffset() {
        return eqOffset + 1;
    }
}
