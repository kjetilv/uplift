package com.github.kjetilv.uplift.fq.paths.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.function.Supplier;

public final class ChannelBufferPuller extends ChannelPuller<ByteBuffer> {

    public ChannelBufferPuller(Path path, byte separator, Supplier<Arena> arena) {
        super(
            path,
            separator,
            arena == null
                ? Arena.ofAuto()
                : arena.get()
        );
    }

    @Override
    protected ByteBuffer bytes(MemorySegment segment, long offset, long length) {
        return segment.asSlice(offset, length).asByteBuffer();
    }
}
