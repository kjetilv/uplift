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
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import static jdk.incubator.vector.VectorOperators.EQ;

@SuppressWarnings("unchecked")
class ByteBufferPuller implements Puller<byte[]> {

    private final Path path;

    private final MemorySegment segment;

    private final RandomAccessFile randomAccessFile;

    private final byte separator;

    private final int endSlice;

    private final long size;

    private long lineStart;

    private long maskStart;

    private VectorMask<Byte> mask = ZERO;

    ByteBufferPuller(Path path, char separator) {
        this.path = Objects.requireNonNull(path, "path");
        this.size = SayFiles.sizeOf(path);
        if (this.size < LENGTH) {
            throw new IllegalStateException(
                "File too small, must be at least " + LENGTH + " bytes: " + path + " (" + size + " bytes)"
            );
        }

        this.randomAccessFile = randomAccessFile(this.path);
        this.segment = segment(randomAccessFile, this.size);
        this.endSlice = Math.toIntExact(LENGTH - size % LENGTH);
        this.separator = (byte) (separator > 0 ? separator : LN);
    }

    @Override
    public byte[] pull() {
        while (true) {
            if (!mask.anyTrue()) {
                if (maskStart > size) {
                    return null;
                }
                nextMask();
            }
            if (mask.anyTrue()) {
                var maskPosition = mask.firstTrue();
                var bytesFound = length(maskPosition);
                var array = bytes(bytesFound);
                lineStart += bytesFound + 1;
                mask = clearMask(maskPosition);
                return array;
            }
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

    private VectorMask<Byte> clearMask(int maskPositions) {
        return mask.and(UNSET[maskPositions]);
    }

    private byte[] bytes(long length) {
        return segment
            .asSlice(lineStart, length)
            .toArray(ValueLayout.JAVA_BYTE);
    }

    private long length(int maskPosition) {
        return maskStart + maskPosition - LENGTH - lineStart;
    }

    private void nextMask() {
        try {
            var byteVector = maskStart + LENGTH <= size
                ? vectorFrom(maskStart)
                : vectorFrom(size - LENGTH).slice(endSlice);
            mask = byteVector.compare(EQ, separator);
        } finally {
            maskStart += LENGTH;
        }
    }

    private ByteVector vectorFrom(long offset) {
        return ByteVector.fromMemorySegment(SPECIES, segment, offset, BYTE_ORDER);
    }

    private static final char LN = '\n';

    private static final ByteOrder BYTE_ORDER = ByteOrder.nativeOrder();

    private static final VectorSpecies<Byte> SPECIES = VectorSpecies.ofPreferred(byte.class);

    private static final int LENGTH = SPECIES.length();

    private static final VectorMask<Byte> ZERO = VectorMask.fromValues(SPECIES, new boolean[LENGTH]);

    @SuppressWarnings("rawtypes")
    private static final VectorMask[] UNSET;

    static {
        UNSET = new VectorMask[LENGTH];
        for (int i = 0; i < LENGTH; i++) {
            UNSET[i] = VectorMask.fromValues(SPECIES, withFalse(i));
        }
    }

    private static boolean[] withFalse(int i) {
        var array = new boolean[LENGTH];
        Arrays.fill(array, true);
        array[i] = false;
        return array;
    }

    private static RandomAccessFile randomAccessFile(Path path) {
        try {
            return new RandomAccessFile(path.toFile(), "r");
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Failed to read " + path, e);
        }
    }

    private static MemorySegment segment(RandomAccessFile file, long size) {
        try {
            return file.getChannel()
                .map(FileChannel.MapMode.READ_ONLY, 0, size, Arena.ofAuto());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to map " + file, e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + path + "]";
    }
}
