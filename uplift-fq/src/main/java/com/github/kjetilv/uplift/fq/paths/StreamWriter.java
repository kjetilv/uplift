package com.github.kjetilv.uplift.fq.paths;

import java.io.OutputStream;

record StreamWriter(OutputStream outputStream) implements Writer<byte[]> {

    @Override
    public Writer<byte[]> write(byte[] line) {
        try {
            outputStream.write(line);
            outputStream.write('\n');
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
