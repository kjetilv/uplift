package com.github.kjetilv.uplift.asynchttp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public record Transfer(long totalSize, int bufferSize) {

    private static final Logger log = LoggerFactory.getLogger(Transfer.class);

    public <B> long copy(
        BufferedReader<B> reader,
        BufferedWriter<? super B> writer
    ) {
        long transferred = 0;
        int turnarounds = 0;
        int emptyTurnarunds = 0;
        int emptyTurnarundsTotal = 0;
        int emptyTurnarundWarnFreq = 2;
        try {
            B buffer = reader.buffer(bufferSize > 0 ? bufferSize : DEFAULT_BUFFER_SIZE);
            while (true) {
                int bytesRead = reader.read(buffer);
                if (bytesRead < 0) {
                    if (transferred == totalSize) {
                        return transferred;
                    }
                    throw new IllegalStateException("EOS after transferring " +
                                                    transferred +
                                                    "/" +
                                                    totalSize +
                                                    " bytes");
                }
                if (bytesRead == 0) {
                    emptyTurnarunds++;
                    emptyTurnarundsTotal++;
                    if (emptyTurnarunds % emptyTurnarundWarnFreq == 0) {
                        log.warn(
                            "{} consecutive empty reads so far, total empty turnarounds: {} ...",
                            emptyTurnarunds,
                            emptyTurnarundsTotal
                        );
                        emptyTurnarundWarnFreq *= 2;
                    }
                } else {
                    long bytesMissing = totalSize - transferred;

                    if (bytesRead > bytesMissing) {
                        log.warn("Received {} bytes, was missing only {}", bytesRead, bytesMissing);
                    }

                    if (emptyTurnarunds > emptyTurnarundWarnFreq) {
                        log.warn("Received {} bytes after {} empty turnarounds ", bytesRead, emptyTurnarunds);
                    }

                    int writableBytes = min(bytesRead, bytesMissing);

                    if (transferred + writableBytes > totalSize) {
                        throw new IllegalStateException(
                            "Transferring " +
                            writableBytes +
                            " bytes on top of already transferred " +
                            transferred +
                            ", would exceed " +
                            totalSize +
                            " wanted, to " +
                            writer +
                            " for " +
                            totalSize +
                            " bytes " +
                            "in " +
                            turnarounds +
                            " turnarounds, empty turnarounds: " +
                            emptyTurnarundsTotal);
                    }

                    try {
                        writer.write(new WritableBuffer<>(buffer, writableBytes));
                    } catch (Exception e) {
                        throw new IllegalStateException(
                            "Failed to write " + bytesRead + " bytes to " + writer + " for " + totalSize + " " +
                            "bytes, transferred " + transferred + " in " + turnarounds + " turnarounds, " +
                            "empty turnarounds: " + emptyTurnarundsTotal, e
                        );
                    } finally {
                        transferred += writableBytes;
                        turnarounds++;
                        emptyTurnarunds = 0;
                        emptyTurnarundWarnFreq = 2;
                    }

                    if (transferred == totalSize) {
                        return transferred;
                    }

                    if (transferred > totalSize) {
                        throw new IllegalStateException(
                            "Transferred " + transferred + " bytes of " + totalSize + " wanted, " +
                            "to " + writer + " for " + totalSize + " bytes " +
                            "in " + turnarounds + " turnarounds, empty turnarounds: " + emptyTurnarundsTotal);
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to write " + totalSize + " bytes to " + writer +
                ", transferred " + transferred + " in " + turnarounds + " turnarounds", e
            );
        }
    }

    private static final int DEFAULT_BUFFER_SIZE = 256 * 1_024;

    private static int min(int bytesRead, long bytesMissing) {
        try {
            return Math.toIntExact(Math.min(bytesMissing, bytesRead));
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected length > " + Integer.MAX_VALUE + ": " + bytesMissing, e);
        }
    }
}














