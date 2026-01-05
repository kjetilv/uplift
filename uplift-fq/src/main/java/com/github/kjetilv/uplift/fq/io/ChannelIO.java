package com.github.kjetilv.uplift.fq.io;

import module java.base;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;

public final class ChannelIO {

    public static <T> RandomAccessFile randomAccessFile(Path source) {
        return randomAccessFile(source, false);
    }

    public static <T> RandomAccessFile randomAccessFile(Path source, boolean writable) {
        var mode = writable ? "rw" : "r";
        try {
            return new RandomAccessFile(source.toFile(), mode);
        } catch (Exception e) {
            throw new IllegalStateException("Failed `" + mode + "`: " + source, e);
        }
    }

    public static MemorySegment memorySegment(RandomAccessFile file) {
        return memorySegment(file, null);
    }

    public static MemorySegment memorySegment(RandomAccessFile file, Arena arena) {
        try {
            return file.getChannel()
                .map(
                    READ_ONLY,
                    0,
                    file.length(),
                    arena == null ? Arena.ofAuto() : arena
                );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to map " + file, e);
        }
    }

    public static void close(RandomAccessFile randomAccessFile, Path source) {
        try {
            randomAccessFile.close();
        } catch (Exception e) {
            throw new IllegalStateException("Could not close " + source, e);
        }
    }

    private ChannelIO() {
    }
}
