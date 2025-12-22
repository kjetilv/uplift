package com.github.kjetilv.uplift.fq.paths.ffm;

import com.github.kjetilv.uplift.fq.paths.Puller;
import com.github.kjetilv.uplift.util.SayFiles;
import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static jdk.incubator.vector.VectorOperators.EQ;

public sealed abstract class ChannelPuller<T> implements Puller<T> permits ChannelArrayPuller, ChannelBufferPuller {

    private final Path path;

    private final MemorySegment segment;

    private final RandomAccessFile randomAccessFile;

    private final byte separator;

    private final int endSlice;

    private final long size;

    private long lineStart;

    private long maskStart;

    private VectorMask<Byte> mask = ZERO;

    public ChannelPuller(Path path, byte separator, Arena arena) {
        this.path = Objects.requireNonNull(path, "path");
        this.separator = separator;
        this.size = SayFiles.sizeOf(path);
        if (this.size < LENGTH) {
            throw new IllegalStateException(
                "File too small, must be at least " + LENGTH + " bytes: " + path + " (" + size + " bytes)"
            );
        }

        this.randomAccessFile = randomAccessFile(this.path);
        this.segment = segment(randomAccessFile, this.size, arena);
        this.endSlice = Math.toIntExact(LENGTH - size % LENGTH);
    }

    @Override
    public T pull() {
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
            var array = bytes(
                segment,
                lineStart,
                bytesFound
            );
            lineStart += bytesFound + 1;
            mask = clearMask(maskPosition);
            return array;
        }
    }

    @Override
    public void close() {
        try {
            randomAccessFile.close();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close " + path, e);
        }
    }

    protected abstract T bytes(MemorySegment segment, long offset, long length);

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
        for (int i = 0; i < LENGTH; i++) {
            UNSET[i] = VectorMask.fromValues(SPECIES, withFalse(i));
        }
    }

    protected static RandomAccessFile randomAccessFile(Path path) {
        try {
            return new RandomAccessFile(path.toFile(), "r");
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Failed to read " + path, e);
        }
    }

    protected static MemorySegment segment(
        RandomAccessFile file,
        long size,
        Arena arena
    ) {
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
        return getClass().getSimpleName() + "[" + path + "]";
    }
}
