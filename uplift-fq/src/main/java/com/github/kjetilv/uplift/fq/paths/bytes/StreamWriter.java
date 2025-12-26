package com.github.kjetilv.uplift.fq.paths.bytes;

import com.github.kjetilv.uplift.fq.paths.Writer;

import java.io.OutputStream;

public record StreamWriter(OutputStream outputStream, byte separator) implements Writer<byte[]> {

    @Override
    public Writer<byte[]> write(byte[] line) {
        try {
            outputStream.write(line);
            outputStream.write(separator);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write line", e);
        }
        return this;
    }

    @Override
    public void close() {
        try {
            outputStream.close();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close", e);
        }
    }
}
