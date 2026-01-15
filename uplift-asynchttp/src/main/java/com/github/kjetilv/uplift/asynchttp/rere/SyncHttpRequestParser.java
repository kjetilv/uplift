package com.github.kjetilv.uplift.asynchttp.rere;

import module java.base;
import module jdk.incubator.vector;

import static jdk.incubator.vector.VectorOperators.EQ;

public final class SyncHttpRequestParser implements Closeable {

    private long lineStart;

    private long maskStart;

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

    public SyncHttpRequestParser(ReadableByteChannel channel, Arena arena, int bufferSize) {
        this.channel = Objects.requireNonNull(channel, "channel");
        this.arena = Objects.requireNonNull(arena, "arena");
        this.bufferSize = bufferSize / 4;
        init();
        fillBuffer();
    }

    public HttpRequest parse() {
        return new HttpRequest(
            parseRequestLine(),
            parseHeaders(),
            body()
        );
    }

    private RequestLine parseRequestLine() {
        int urlIndex = 0;
        while (true) {
            if (spaceMask.firstTrue() == LENGTH) {
                nextSpaceMask();
            }
            var maskPosition = spaceMask.firstTrue();
            if (maskPosition == LENGTH) {
                continue;
            }
            var bytesFound = Math.toIntExact(maskStart + maskPosition - LENGTH);
            if (urlIndex == 0) {
                urlIndex = bytesFound;
                lineMask = unset(lineMask, 1 + maskPosition);
                spaceMask = unset(spaceMask, 1 + maskPosition);
                continue;
            }
            while (true) {
                if (lineMask.firstTrue() == LENGTH) {
                    nextLineMask();
                }
                var linePos = lineMask.firstTrue();
                if (linePos == LENGTH) {
                    continue;
                }
                return requestLine(linePos, urlIndex, bytesFound);
            }
        }
    }

    private RequestLine requestLine(int linePos, int urlIndex, int bytesFound) {
        lineMask = unset(lineMask, 1 + linePos);
        var lineBreak = Math.toIntExact(maskStart - LENGTH + linePos);
        lineStart = lineBreak + 1;
        return new RequestLine(
            memorySegment,
            urlIndex + 1,
            bytesFound + 1,
            lineBreak + 1
        );
    }

    public RequestHeader parseHeader() {
        while (true) {
            if (lineStart == available && done) {
                return null;
            }
            if (lineMask.firstTrue() == LENGTH) {
                // Drained mask
                nextLineMask();
            }
            var maskPosition = lineMask.firstTrue();
            if (maskPosition == LENGTH) {
                // Drained mask
                continue;
            }
            var bytesFound = maskStart + maskPosition - LENGTH - lineStart;
            if (bytesFound == 0) {
                // Empty line, end of headers section
                bodyStart = Math.toIntExact(lineStart + bodyStart + 1);
                return null;
            }
            var separatorOffset = separatorOffset();
            var httpHeader = new RequestHeader(
                memorySegment,
                Math.toIntExact(lineStart),
                Math.toIntExact(separatorOffset),
                Math.toIntExact(bytesFound)
            );
            lineStart += bytesFound + 1;
            lineMask = unset(lineMask, 1 + maskPosition);
            return httpHeader;
        }
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close " + channel, e);
        }
    }

    private ReadableByteChannel body() {
        int bufferedBodyBytes = available - bodyStart;
        if (bufferedBodyBytes <= 0) {
            return channel;
        }
        var bufferedBody = memorySegment.asSlice(bodyStart, bufferedBodyBytes).asByteBuffer();
        return new ReadableByteChannel() {

            @Override
            public int read(ByteBuffer dst) {
                if (bufferedBody.hasRemaining()) {
                    int toWrite = getToWrite(dst, bufferedBody);
                    if (toWrite > 0) {
                        int oldLimit = bufferedBody.limit();
                        bufferedBody.limit(bufferedBody.position() + toWrite);
                        dst.put(bufferedBody);
                        bufferedBody.limit(oldLimit);
                        return toWrite;
                    }
                }
                try {
                    return channel.read(dst);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to read from " + channel, e);
                }
            }

            @Override
            public boolean isOpen() {
                return bufferedBody.hasRemaining() || channel.isOpen();
            }

            @Override
            public void close() {
                try {
                    channel.close();
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to close " + channel, e);
                }
            }
        };
    }

    private static int getToWrite(ByteBuffer dst, ByteBuffer bufferedBody) {
        return Math.min(dst.remaining(), bufferedBody.remaining());
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
        this.buffer.position(Math.toIntExact(maskStart));
    }

    private int read() {
        try {
            return this.channel.read(this.buffer);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read from " + this.channel, e);
        }
    }

    private long separatorOffset() {
        long start = lineStart;
        while (true) {
            var byteVector = vectorFrom(start);
            var vectorMask = byteVector.compare(EQ, ':');
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
        var lineVector = vectorFrom(maskStart); //atEnd() ? endSlice() : slice();
        spaceMask = lineVector.compare(EQ, ' ');
        lineMask = lineVector.compare(EQ, (byte) '\n');
        maskStart += LENGTH;
    }

    private void nextLineMask() {
        refill();
        var lineVector = vectorFrom(maskStart); //atEnd() ? endSlice() : slice();
        lineMask = lineVector.compare(EQ, (byte) '\n');
        maskStart += LENGTH;
    }

    private ByteVector vectorFrom(long maskStart) {
        return ByteVector.fromMemorySegment(SPECIES, memorySegment, maskStart, BYTE_ORDER);
    }

    private void refill() {
        if (maskStart == bufferSize) {
            expand();
            fillBuffer();
        }
    }

    private void fillBuffer() {
        while (available < bufferSize && !done) {
            switch (read()) {
                case -1 -> {
                    done = true;
                    return;
                }
                case int read -> available += read;
            }
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
