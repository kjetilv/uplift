package com.github.kjetilv.uplift.fq.paths.ffm;

import com.github.kjetilv.uplift.fq.AccessProvider;
import com.github.kjetilv.uplift.fq.paths.PathTombstone;
import com.github.kjetilv.uplift.fq.paths.Puller;
import com.github.kjetilv.uplift.fq.paths.Tombstone;
import com.github.kjetilv.uplift.fq.paths.Writer;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ChannelAccessProvider<T>
    implements AccessProvider<Path, T> {

    private final Supplier<Arena> arena;

    private final byte separator;

    private final Function<MemorySegment, T> fromMemorySegment;

    private final Function<T, ByteBuffer> toByteBuffer;

    private final Supplier<ByteBuffer> linebreak;

    public ChannelAccessProvider(
        Supplier<Arena> arena,
        byte separator,
        Function<MemorySegment, T> fromMemorySegment,
        Function<T, ByteBuffer> toByteBuffer,
        Supplier<ByteBuffer> linebreak
    ) {
        this.arena = arena == null ? Arena::ofAuto : arena;
        this.separator = separator > 0 ? (byte) '\n' : separator;
        this.fromMemorySegment = Objects.requireNonNull(fromMemorySegment, "fromMemorySegment");
        this.toByteBuffer = Objects.requireNonNull(toByteBuffer, "toByteBuffer");
        this.linebreak = Objects.requireNonNull(linebreak, "linebreak");
    }

    @Override
    public Puller<T> puller(Path path) {
        return new ChannelPuller<>(path, separator, arena.get(), fromMemorySegment);
    }

    @Override
    public Writer<T> writer(Path path) {
        return new ChannelWriter<>(path, toByteBuffer, linebreak);
    }

    @Override
    public Tombstone<Path> tombstone(Path path) {
        return new PathTombstone(path.resolve("done"));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[]";
    }

}
