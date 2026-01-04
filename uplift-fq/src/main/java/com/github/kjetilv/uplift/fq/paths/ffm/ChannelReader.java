package com.github.kjetilv.uplift.fq.paths.ffm;

import com.github.kjetilv.uplift.fq.paths.Reader;
import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.function.Function;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.util.Objects.requireNonNull;
import static jdk.incubator.vector.VectorOperators.EQ;

class ChannelReader<T> implements Reader<T> {

    private final MemorySegment segment;

    private final RandomAccessFile randomAccessFile;

    private final byte separator;

    private final Function<MemorySegment, T> mapper;

    private final int endSlice;

    private final long size;

    private long lineStart;

    private long maskStart;

    private VectorMask<Byte> mask = ZERO;

    ChannelReader(
        RandomAccessFile randomAccessFile,
        byte separator,
        Arena arena,
        Function<MemorySegment, T> mapper
    ) {
        this.separator = separator;
        requireNonNull(arena, "arena");
        this.mapper = requireNonNull(mapper, "mapper");
        this.randomAccessFile = requireNonNull(randomAccessFile, "randomAccessFile");
        try {
            this.size = randomAccessFile.length();
        } catch (Exception e) {
            throw new IllegalStateException("Could not find size of " + randomAccessFile, e);
        }
        if (this.size < LENGTH) {
            throw new IllegalArgumentException("Invalid size of " + randomAccessFile + ": " + size + "<" + LENGTH);
        }
        this.segment = segment(randomAccessFile, this.size, arena);
        this.endSlice = Math.toIntExact(LENGTH - size % LENGTH);
    }

    @Override
    public T read() {
        while (true) {
            if (mask.firstTrue() == LENGTH) {
                if (maskStart > size) {
                    return null;
                }
                nextMask();
            }
            var maskPosition = mask.firstTrue();
            if (maskPosition == LENGTH) {
                continue;
            }
            var bytesFound = length(maskPosition);
            var segment = this.segment.asSlice(lineStart, bytesFound);
            var t = mapper.apply(segment);
            lineStart += bytesFound + 1;
            mask = clearMask(maskPosition);
            return t;
        }
    }

    @Override
    public void close() {
        try {
            randomAccessFile.close();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close " + randomAccessFile, e);
        }
    }

    @SuppressWarnings("unchecked")
    private VectorMask<Byte> clearMask(int maskPositions) {
        return mask.and(UNSET[maskPositions]);
    }

    private long length(int maskPosition) {
        return maskStart + maskPosition - LENGTH - lineStart;
    }

    private void nextMask() {
        try {
            var vector = maskStart + LENGTH <= size
                ? vectorFrom(maskStart)
                : vectorFrom(size - LENGTH).slice(endSlice);
            mask = vector.compare(EQ, separator);
        } finally {
            maskStart += LENGTH;
        }
    }

    private ByteVector vectorFrom(long offset) {
        return ByteVector.fromMemorySegment(SPECIES, segment, offset, BYTE_ORDER);
    }

    private static final VectorSpecies<Byte> SPECIES = VectorSpecies.ofPreferred(byte.class);

    private static final int LENGTH = SPECIES.length();

    private static final VectorMask<Byte> ZERO = VectorMask.fromValues(SPECIES, new boolean[LENGTH]);

    private static final ByteOrder BYTE_ORDER = ByteOrder.nativeOrder();

    @SuppressWarnings("rawtypes")
    private static final VectorMask[] UNSET;

    static {
        UNSET = new VectorMask[LENGTH];
        for (var i = 0; i < LENGTH; i++) {
            UNSET[i] = VectorMask.fromValues(SPECIES, withFalse(i));
        }
    }

    protected static MemorySegment segment(RandomAccessFile file, long size, Arena arena) {
        try {
            return file.getChannel()
                .map(READ_ONLY, 0, size, arena);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to map " + file, e);
        }
    }

    private static boolean[] withFalse(int i) {
        var array = new boolean[LENGTH];
        Arrays.fill(array, true);
        array[i] = false;
        return array;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + randomAccessFile + "]";
    }
}
