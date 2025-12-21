package com.github.kjetilv.uplift.fq.paths.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.file.Path;

class ChannelArrayPuller extends ChannelPuller<byte[]> {

    ChannelArrayPuller(Path path, byte separator, Arena arena) {
        super(path, separator, arena);
    }

    @Override
    protected byte[] bytes(MemorySegment segment, long offset, long length) {
        return segment.asSlice(offset, length).toArray(ValueLayout.JAVA_BYTE);
    }
}
