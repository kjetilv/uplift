package com.github.kjetilv.uplift.asynchttp.rere;

import module java.base;
import module jdk.incubator.vector;

public final class SyncHttpRequestReader implements Closeable {

    private int lineStart;

    private int maskEnd;

    private VectorMask<Byte> lineMask = ZERO;

    private VectorMask<Byte> spaceMask = ZERO;

    private int available;

    private int bufferSize;

    private int doubled;

    private ByteBuffer buffer;

    private MemorySegment memorySegment;

    private final ReadableByteChannel channel;

    private final Arena arena;

    private boolean done;

    private int bodyStart;

    public SyncHttpRequestReader(ReadableByteChannel channel, Arena arena, int bufferSize) {
        this.channel = Objects.requireNonNull(channel, "channel");
        this.arena = Objects.requireNonNull(arena, "arena");
        this.bufferSize = bufferSize / 4;
        if (this.bufferSize % LENGTH != 0) {
            throw new IllegalStateException("Require bufferSize/4 divisible by " + LENGTH + ": " + bufferSize);
        }
    }

    public HttpRequest read() {
        init();
        fillBuffer();
        var requestLine = parseRequestLine();
        var headers = parseHeaders();
        var body = body();
        return new HttpRequest(
            requestLine,
            headers,
            body
        );
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close " + channel, e);
        }
    }

    private RequestLine parseRequestLine() {
        int urlIndex = 0;
        while (true) {
            if (spaceMask.firstTrue() == LENGTH) {
                nextSpaceMask();
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
                    nextLineMask();
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

    private RequestHeader parseHeader() {
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
                    nextLineMask();
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
            var httpHeader = new RequestHeader(
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

    private RequestLine requestLine(int lineMaskPos, int urlIndex, int bytesFound) {
        lineMask = unset(lineMask, lineMaskPos + 1);
        var lineBreak = Math.toIntExact(maskEnd - LENGTH + lineMaskPos);
        lineStart = lineBreak + 1;
        var cr = isCr(lineBreak - 1) ? 1 : 0;
        return new RequestLine(
            memorySegment,
            urlIndex + 1,
            bytesFound + 1,
            lineBreak + 1 - cr
        );
    }

    private boolean isCr(long offset) {
        return memorySegment.get(ValueLayout.JAVA_BYTE, offset) == '\r';
    }

    private ReadableByteChannel body() {
        int bufferedBodyBytes = available - bodyStart;
        return bufferedBodyBytes <= 0
            ? channel
            : new BodyBytes(
                channel,
                memorySegment.asSlice(bodyStart, bufferedBodyBytes).asByteBuffer()
            );
    }

    private List<RequestHeader> parseHeaders() {
        List<RequestHeader> headers = new ArrayList<>();
        while (true) {
            var httpHeader = parseHeader();
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

    private void nextSpaceMask() {
        refill();
        var vector = vectorFrom(maskEnd); //atEnd() ? endSlice() : slice();
        spaceMask = vector.compare(VectorOperators.EQ, ' ');
        lineMask = vector.compare(VectorOperators.EQ, (byte) '\n');
        maskEnd += LENGTH;
    }

    private void nextLineMask() {
        refill();
        var lineVector = vectorFrom(maskEnd); //atEnd() ? endSlice() : slice();
        lineMask = lineVector.compare(VectorOperators.EQ, (byte) '\n');
        maskEnd += LENGTH;
    }

    private ByteVector vectorFrom(long maskStart) {
        return ByteVector.fromMemorySegment(SPECIES, memorySegment, maskStart, BYTE_ORDER);
    }

    private void refill() {
        if (maskEnd >= bufferSize) {
            expand();
        }
        if (maskEnd >= available) {
            fillBuffer();
        }
    }

    private void fillBuffer() {
        while (available < bufferSize && !done) {
            switch (readIntoBuffer()) {
                case -1 -> done = true;
                case int read -> {
                    available += read;
                    return;
                }
            }
        }
    }

    private int readIntoBuffer() {
        try {
            return this.channel.read(this.buffer);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read from " + this.channel, e);
        }
    }

    private static final VectorSpecies<Byte> SPECIES = VectorSpecies.ofPreferred(byte.class);

    private static final int LENGTH = SPECIES.length();

    private static final VectorMask<Byte> ZERO = VectorMask.fromValues(SPECIES, new boolean[LENGTH]);

    private static final ByteOrder BYTE_ORDER = ByteOrder.nativeOrder();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + channel + "]";
    }
}
