package com.github.kjetilv.uplift.fq.paths.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.function.Supplier;

final class ChannelBufferPuller
    extends ChannelPuller<ByteBuffer> {

    ChannelBufferPuller(Path path, byte separator, Supplier<Arena> arena) {
        super(path, separator, arena.get());
    }

    @Override
    protected ByteBuffer bytes(MemorySegment segment, long offset, long length) {
        return segment.asSlice(offset, length).asByteBuffer();
    }
}
