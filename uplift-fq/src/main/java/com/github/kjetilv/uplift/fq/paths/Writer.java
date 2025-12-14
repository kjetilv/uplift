package com.github.kjetilv.uplift.fq.paths;

import java.io.BufferedWriter;

record Writer(BufferedWriter bufferedWriter) {

    Writer write(String line) {
        try {
            bufferedWriter.write(line);
            bufferedWriter.write("\n");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write line", e);
        }
        return this;
    }

    void close() {
        try {
            bufferedWriter.close();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close", e);
        }
    }
}
