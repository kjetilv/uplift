package com.github.kjetilv.uplift.fq.paths.ffm;

import com.github.kjetilv.uplift.fq.AccessProvider;
import com.github.kjetilv.uplift.fq.paths.PathTombstone;
import com.github.kjetilv.uplift.fq.paths.Puller;
import com.github.kjetilv.uplift.fq.paths.Tombstone;
import com.github.kjetilv.uplift.fq.paths.Writer;

import java.lang.foreign.Arena;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.function.Supplier;

 public record ChannelBufferAccessProvider(Supplier<Arena> arena, byte separator)
    implements AccessProvider<Path, ByteBuffer> {

    public ChannelBufferAccessProvider() {
        this('\n', null);
    }

    ChannelBufferAccessProvider(char separator, Supplier<Arena> arena) {
        this(arena, (byte) separator);
    }

    public ChannelBufferAccessProvider(Supplier<Arena> arena, byte separator) {
        this.arena = arena;
        this.separator = separator > 0 ? (byte) '\n' : separator;
    }

    @Override
    public Puller<ByteBuffer> puller(Path path) {
        return new ChannelBufferPuller(path, this.separator(), this.arena());
    }

    @Override
    public Writer<ByteBuffer> writer(Path path) {
        return new ChannelBufferWriter(path, this.separator());
    }

    @Override
    public Tombstone<Path> tombstone(Path path) {
        return new PathTombstone(path.resolve("done"));
    }

}
