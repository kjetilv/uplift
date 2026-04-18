package com.github.kjetilv.uplift.synchttp.rere;

import module java.base;
import com.github.kjetilv.uplift.synchttp.Utils;

public record QueryParameter(MemorySegment segment, long offset, long eqOffset, long length) {

    public String name() {
        return Utils.string(segment, offset, nameLength());
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

    @Override
    public String toString() {
        return name() + "=" + value();
    }
}
