package com.github.kjetilv.uplift.fq.paths.ffm;

import com.github.kjetilv.uplift.util.SayFiles;
import com.github.kjetilv.uplift.fq.paths.Puller;
import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static jdk.incubator.vector.VectorOperators.EQ;

class ByteBufferPuller implements Puller<byte[]> {

    private final Path path;

    private final MemorySegment memorySegment;

    private final RandomAccessFile randomAccessFile;

    private final long size;

    private long lineStart;

    private long maskStart;

    private VectorMask<Byte> mask = ZERO;

    ByteBufferPuller(Path path) {
        this.path = Objects.requireNonNull(path, "path");
        try {
            this.randomAccessFile = new RandomAccessFile(this.path.toFile(), "r");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            this.size = SayFiles.sizeOf(path);
            this.memorySegment = randomAccessFile.getChannel()
                .map(
                    READ_ONLY,
                    0,
                    this.size,
                    Arena.ofAuto()
                );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] pull() {
        while (true) {
            if (!mask.anyTrue()) {
                if (maskStart > size) {
                    return null;
                }
                mask = newMask();
            }
            if (mask.anyTrue()) {
                var firstTrue = mask.firstTrue();
                var next = maskStart - SPECIES.length() + firstTrue;
                var length = next - lineStart;
                var array = array(length);
                lineStart += length + 1;
                mask = mask.and(UNSETS[firstTrue]);
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

    @Override
    public Path path() {
        return path;
    }

    private byte[] array(long length) {
        var segment = memorySegment.asSlice(lineStart, length);
        return segment.toArray(ValueLayout.JAVA_BYTE);
    }

    private VectorMask<Byte> newMask() {
        try {
            return atEnd()
                ? vectorFrom(size - SPECIES.length()).slice(shift()).compare(EQ, (byte) '\n')
                : vectorFrom(maskStart).compare(EQ, (byte) '\n');
        } finally {
            maskStart += SPECIES.length();
        }
    }

    private ByteVector vectorFrom(long offset) {
        return ByteVector.fromMemorySegment(
            SPECIES,
            memorySegment,
            offset,
            BYTE_ORDER
        );
    }

    private int shift() {
        return Math.toIntExact(SPECIES.length() - size % SPECIES.length());
    }

    private boolean atEnd() {
        return maskStart + SPECIES.length() > size;
    }

    private static final ByteOrder BYTE_ORDER = ByteOrder.nativeOrder();

    private static final VectorSpecies<Byte> SPECIES = VectorSpecies.ofPreferred(byte.class);

    private static final VectorMask<Byte>[] UNSETS;

    private static final VectorMask<Byte> ZERO =
        VectorMask.fromValues(SPECIES, new boolean[SPECIES.length()]);

    static {
        var unsets = new ArrayList<VectorMask<Byte>>(SPECIES.length());
        for (int i = 0; i < SPECIES.length(); i++) {
            var copy = new boolean[SPECIES.length()];
            Arrays.fill(copy, true);
            copy[i] = false;
            var byteVectorMask = VectorMask.fromValues(SPECIES, copy);
            unsets.add(byteVectorMask);
        }
        //noinspection unchecked
        UNSETS = unsets.<VectorMask<Byte>>toArray(new VectorMask[0]);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + path + "]";
    }
}
