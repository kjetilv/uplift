package com.github.kjetilv.uplift.fq.paths.ffm;

import com.github.kjetilv.uplift.fq.AccessProvider;
import com.github.kjetilv.uplift.fq.paths.Reader;
import com.github.kjetilv.uplift.fq.paths.Writer;
import jdk.incubator.vector.VectorSpecies;

import java.io.RandomAccessFile;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.kjetilv.uplift.util.SayFiles.sizeOf;
import static java.util.Objects.requireNonNull;

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
        this.fromMemorySegment = requireNonNull(fromMemorySegment, "fromMemorySegment");
        this.toByteBuffer = requireNonNull(toByteBuffer, "toByteBuffer");
        this.linebreak = requireNonNull(linebreak, "linebreak");
    }

    @Override
    public Reader<T> reader(Path source) {
        var size = sizeOf(source);
        if (size < LENGTH) {
            throw new IllegalStateException(
                "File too small, must be at least " + LENGTH + " bytes: " + source + " (" + size + " bytes)");
        }
        return new ChannelReader<>(
            randomAccess(source, "r"),
            separator,
            arena.get(),
            fromMemorySegment
        );
    }

    @Override
    public Writer<T> writer(Path source) {
        return new ChannelWriter<>(
            randomAccess(source, "rw"),
            toByteBuffer,
            linebreak
        );
    }

    private static final VectorSpecies<Byte> SPECIES =
        VectorSpecies.ofPreferred(byte.class);

    private static final int LENGTH = SPECIES.length();

    private static <T> RandomAccessFile randomAccess(Path source, String mode) {
        try {
            return new RandomAccessFile(source.toFile(), mode);
        } catch (Exception e) {
            throw new IllegalStateException("Failed " + mode + ": " + source, e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[]";
    }
}
