package com.github.kjetilv.uplift.fq.io;

import module java.base;
import module jdk.incubator.vector;

import com.github.kjetilv.uplift.util.RuntimeCloseable;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;

public final class LinesReader implements Supplier<MemorySegment>, RuntimeCloseable {

    private final MemorySegment segment;

    private final RandomAccessFile randomAccessFile;

    private final byte separator;

    private final int endSlice;

    private final long size;

    private long lineStart;

    private long maskStart;

    private VectorMask<Byte> mask = ZERO;

    public LinesReader(RandomAccessFile randomAccessFile, byte separator, Arena arena) {
        this.randomAccessFile = Objects.requireNonNull(randomAccessFile, "randomAccessFile");
        this.separator = separator;
        Objects.requireNonNull(arena, "arena");
        try {
            this.size = randomAccessFile.length();
        } catch (Exception e) {
            throw new IllegalStateException("Could not find size of " + randomAccessFile, e);
        }
        if (this.size < LENGTH) {
            throw new IllegalArgumentException("Invalid size of " + randomAccessFile + ": " + size + "<" + LENGTH);
        }
        this.segment = memorySegment(randomAccessFile, arena);
        this.endSlice = Math.toIntExact(LENGTH - size % LENGTH);
    }

    @Override
    public MemorySegment get() {
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
            lineStart += bytesFound + 1;
            mask = clearMask(maskPosition);
            return segment;
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
            mask = vector.compare(VectorOperators.EQ, separator);
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

    private static MemorySegment memorySegment(RandomAccessFile file, Arena arena) {
        try {
            return file.getChannel()
                .map(
                    READ_ONLY,
                    0,
                    file.length(),
                    arena == null ? Arena.ofAuto() : arena
                );
        } catch (Exception e) {
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
