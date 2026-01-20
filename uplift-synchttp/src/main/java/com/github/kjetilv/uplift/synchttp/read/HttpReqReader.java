package com.github.kjetilv.uplift.synchttp.read;

import module java.base;
import module jdk.incubator.vector;
import com.github.kjetilv.uplift.synchttp.req.HttpReq;
import com.github.kjetilv.uplift.synchttp.req.ReqHeader;
import com.github.kjetilv.uplift.synchttp.req.ReqLine;

public final class HttpReqReader {

    private int lineStart;

    private int maskEnd;

    private VectorMask<Byte> lineMask = ZERO;

    private VectorMask<Byte> spaceMask = ZERO;

    private int available;

    private int bufferSize;

    private int doubled;

    private ByteBuffer buffer;

    private MemorySegment memorySegment;

    private final Arena arena;

    private boolean done;

    private int bodyStart;

    public HttpReqReader(Arena arena, int bufferSize) {
        this.arena = Objects.requireNonNull(arena, "arena");
        this.bufferSize = bufferSize / 4;
        if (this.bufferSize % LENGTH != 0) {
            throw new IllegalStateException("Require bufferSize/4 divisible by " + LENGTH + ": " + bufferSize);
        }
    }

    public HttpReq read(ReadableByteChannel channel) {
        try {
            init();
            fillBuffer(channel);
            var requestLine = parseRequestLine(channel);
            var headers = parseHeaders(channel);
            var body = body(channel);
            return new HttpReq(requestLine, headers, body);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read from " + channel, e);
        }
    }

    private ReqLine parseRequestLine(ReadableByteChannel channel) {
        int urlIndex = 0;
        while (true) {
            if (spaceMask.firstTrue() == LENGTH) {
                nextSpaceMask(channel);
            }
            var spaceMaskPos = spaceMask.firstTrue();
            if (spaceMaskPos == LENGTH) {
                continue;
            }
            var bytesFound = Math.toIntExact(maskEnd + spaceMaskPos - LENGTH);
            if (urlIndex == 0) {
                urlIndex = bytesFound;
                spaceMask = unset(spaceMask, 1 + spaceMaskPos);
                continue;
            }
            while (true) {
                if (lineMask.firstTrue() == LENGTH) {
                    nextLineMask(channel);
                }
                var lineMaskPos = lineMask.firstTrue();
                if (lineMaskPos == LENGTH) {
                    continue;
                }
                return requestLine(
                    lineMaskPos,
                    urlIndex,
                    bytesFound
                );
            }
        }
    }

    private ReqHeader parseHeader(ReadableByteChannel channel) {
        while (true) {
            if (lineStart >= available && done) {
                bodyStart = Math.toIntExact(lineStart + 1);
                return null;
            }
            int lineMaskPos = lineMask.firstTrue();
            int bytesFound;
            if (lineMaskPos == LENGTH) {
                // No hits in mask
                if (maskEnd >= available && done) {
                    // All data read,
                    bytesFound = available - lineStart;
                } else {
                    // Drained mask
                    nextLineMask(channel);
                    lineMaskPos = lineMask.firstTrue();
                    if (lineMaskPos == LENGTH) {
                        // Drained mask
                        continue;
                    }
                    bytesFound = maskEnd + lineMaskPos - LENGTH - lineStart;
                }
            } else {
                bytesFound = maskEnd + lineMaskPos - LENGTH - lineStart;
            }
            var cr = isCr(lineStart + bytesFound - 1) ? 1 : 0;
            if (bytesFound - cr == 0) {
                // Empty line, end of headers section
                bodyStart = Math.toIntExact(lineStart + 1 + cr);
                return null;
            }
            var separatorOffset = separatorOffset();
            var httpHeader = new ReqHeader(
                memorySegment,
                Math.toIntExact(lineStart),
                Math.toIntExact(separatorOffset),
                Math.toIntExact(bytesFound - cr)
            );
            lineStart += bytesFound + 1;
            lineMask = unset(lineMask, 1 + lineMaskPos);
            return httpHeader;
        }
    }

    private ReqLine requestLine(int lineMaskPos, int urlIndex, int bytesFound) {
        lineMask = unset(lineMask, lineMaskPos + 1);
        var lineBreak = Math.toIntExact(maskEnd - LENGTH + lineMaskPos);
        lineStart = lineBreak + 1;
        var cr = isCr(lineBreak - 1) ? 1 : 0;
        return new ReqLine(
            memorySegment,
            urlIndex + 1,
            bytesFound + 1,
            lineBreak + 1 - cr
        );
    }

    private boolean isCr(long offset) {
        return memorySegment.get(ValueLayout.JAVA_BYTE, offset) == '\r';
    }

    private ReadableByteChannel body(ReadableByteChannel channel) {
        int bufferedBodyBytes = available - bodyStart;
        return bufferedBodyBytes > 0
            ? bodyBytes(channel, bufferedBodyBytes)
            : channel;
    }

    private BodyBytes bodyBytes(ReadableByteChannel channel, int bufferedBodyBytes) {
        var segment = memorySegment.asSlice(bodyStart, bufferedBodyBytes);
        return new BodyBytes(channel, segment.asByteBuffer());
    }

    private List<ReqHeader> parseHeaders(ReadableByteChannel channel) {
        List<ReqHeader> headers = new ArrayList<>();
        while (true) {
            var httpHeader = parseHeader(channel);
            if (httpHeader == null) {
                return headers;
            }
            headers.add(httpHeader);
        }
    }

    private void init() {
        this.memorySegment = arena.allocate(ValueLayout.JAVA_BYTE, bufferSize);
        this.buffer = this.memorySegment.asByteBuffer();
    }

    private void expand() {
        if (doubled == 2) {
            throw new IllegalStateException("Buffer size exhausted: " + bufferSize);
        }
        bufferSize *= 2;
        doubled++;
        var oldSegment = memorySegment;
        this.memorySegment = arena.allocate(ValueLayout.JAVA_BYTE, bufferSize);
        this.memorySegment.copyFrom(oldSegment);
        this.buffer = memorySegment.asByteBuffer();
        this.buffer.position(Math.toIntExact(maskEnd));
    }

    private long separatorOffset() {
        long start = lineStart;
        while (true) {
            var byteVector = vectorFrom(start);
            var vectorMask = byteVector.compare(VectorOperators.EQ, ':');
            var firstTrue = vectorMask.firstTrue();
            if (firstTrue != LENGTH) {
                return start + firstTrue;
            }
            start += LENGTH;
        }
    }

    private VectorMask<Byte> unset(VectorMask<Byte> mask, int pos) {
        return mask.indexInRange(-pos, LENGTH - pos);
    }

    private void nextSpaceMask(ReadableByteChannel channel) {
        refill(channel);
        var vector = vectorFrom(maskEnd); //atEnd() ? endSlice() : slice();
        spaceMask = vector.compare(VectorOperators.EQ, ' ');
        lineMask = vector.compare(VectorOperators.EQ, (byte) '\n');
        maskEnd += LENGTH;
    }

    private void nextLineMask(ReadableByteChannel channel) {
        refill(channel);
        var lineVector = vectorFrom(maskEnd); //atEnd() ? endSlice() : slice();
        lineMask = lineVector.compare(VectorOperators.EQ, (byte) '\n');
        maskEnd += LENGTH;
    }

    private ByteVector vectorFrom(long maskStart) {
        return ByteVector.fromMemorySegment(
            SPECIES,
            memorySegment,
            maskStart,
            BYTE_ORDER
        );
    }

    private void refill(ReadableByteChannel channel) {
        if (maskEnd >= bufferSize) {
            expand();
        }
        if (maskEnd >= available) {
            fillBuffer(channel);
        }
    }

    private void fillBuffer(ReadableByteChannel channel) {
        while (available < bufferSize && !done) {
            switch (readIntoBuffer(channel)) {
                case -1 -> done = true;
                case int read -> {
                    available += read;
                    return;
                }
            }
        }
    }

    private int readIntoBuffer(ReadableByteChannel channel) {
        try {
            return channel.read(this.buffer);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read from " + channel, e);
        }
    }

    private static final VectorSpecies<Byte> SPECIES = VectorSpecies.ofPreferred(byte.class);

    private static final int LENGTH = SPECIES.length();

    private static final VectorMask<Byte> ZERO = VectorMask.fromValues(SPECIES, new boolean[LENGTH]);

    private static final ByteOrder BYTE_ORDER = ByteOrder.nativeOrder();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[]";
    }
}
