package com.github.kjetilv.uplift.fq.io;

import module java.base;

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
