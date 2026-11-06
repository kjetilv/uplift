package com.github.kjetilv.uplift.synchttp.rere;

import module java.base;
import com.github.kjetilv.uplift.synchttp.Utils;

/// A query parameter. [#name()] and [#value()] are decoded: `%XX` escapes, and `+` as space.
public record QueryParameter(MemorySegment segment, long offset, long eqOffset, long length) {

    public String name() {
        return Utils.decode(segment, offset, nameLength());
    }

    public String value() {
        return length == 0 ? "" : Utils.decode(segment, valueOffset(), length);
    }

    public long nameLength() {
        return eqOffset - offset;
    }

    /// @param name Decoded name
    /// @return True if this parameter has the name
    public boolean hasName(String name) {
        return nameLength() == name.length() && Utils.isEqual(name, segment, offset) ||
               Utils.encoded(segment, offset, nameLength()) && name().equals(name);
    }

    private long valueOffset() {
        return eqOffset + 1;
    }

    @Override
    public String toString() {
        return name() + "=" + value();
    }
}
