package com.github.kjetilv.uplift.fq.paths.ffm;

import com.github.kjetilv.uplift.fq.AccessProvider;
import com.github.kjetilv.uplift.fq.Tombstone;
import com.github.kjetilv.uplift.fq.paths.PathTombstone;
import com.github.kjetilv.uplift.fq.paths.Puller;
import com.github.kjetilv.uplift.fq.paths.Writer;

import java.lang.foreign.Arena;
import java.nio.file.Path;
import java.util.function.Supplier;

public record ChannelsAccessProvider(Supplier<Arena> arena, byte separator)
    implements AccessProvider<Path, byte[]> {

    public ChannelsAccessProvider() {
        this('\n', null);
    }

    public ChannelsAccessProvider(Supplier<Arena> arena) {
        this((char) 0, arena);
    }

    public ChannelsAccessProvider(char separator, Supplier<Arena> arena) {
        this(arena, (byte) separator);
    }

    public ChannelsAccessProvider(Supplier<Arena> arena, byte separator) {
        this.arena = arena;
        this.separator = separator > 0 ? (byte) '\n' : separator;
    }

    @Override
    public Puller<byte[]> puller(Path path) {
        return new ByteBufferPuller(
            path,
            separator,
            arena == null ? Arena.ofAuto() : arena.get()
        );
    }

    @Override
    public Writer<byte[]> writer(Path path) {
        return new ByteBufferWriter(path, separator);
    }

    @Override
    public Tombstone<Path> tombstone(Path path) {
        return new PathTombstone(path.resolve("done"));
    }
}
