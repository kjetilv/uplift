package com.github.kjetilv.uplift.synchttp.read;

import module java.base;
import module jdk.incubator.vector;
import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import com.github.kjetilv.uplift.synchttp.rere.ReqHeader;
import com.github.kjetilv.uplift.synchttp.rere.ReqHeaders;
import com.github.kjetilv.uplift.synchttp.rere.ReqLine;

public final class HttpReqReader {

    public static HttpReqReader defaultReader() {
        return new HttpReqReader(new Segments());
    }

    private int lineStart;

    private int maskEnd;

    private VectorMask<Byte> lineMask = ZERO;

    private VectorMask<Byte> spaceMask = ZERO;

    private int available;

    private long bufferSize;

    private ByteBuffer buffer;

    private Segments.Pooled pooled;

    private final Segments segments;

    private boolean done;

    private int bodyStart;

    public HttpReqReader(Segments segments) {
        this.segments = Objects.requireNonNull(segments, "segments");
    }

    public HttpReq read(ReadableByteChannel channel) {
        try {
            init();
            fillBuffer(channel);
            if (available == 0 && done) {
                return null;
            }
            var requestLine = parseRequestLine(channel);
            var headers = parseHeaders(channel);
            var body = body(channel);
            var reqHeaders = new ReqHeaders(headers.toArray(ReqHeader[]::new));
            return new HttpReq(requestLine, reqHeaders, body, this::release);
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
                pooled.segment(),
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
            pooled.segment(),
            urlIndex + 1,
            bytesFound + 1,
            lineBreak + 1 - cr
        );
    }

    private boolean isCr(long offset) {
        return pooled.segment().get(ValueLayout.JAVA_BYTE, offset) == '\r';
    }

    private ReadableByteChannel body(ReadableByteChannel channel) {
        int bufferedBodyBytes = available - bodyStart;
        return bufferedBodyBytes > 0
            ? bodyBytes(channel, bufferedBodyBytes)
            : Channels.newChannel(InputStream.nullInputStream());
    }

    private BodyBytes bodyBytes(ReadableByteChannel channel, int bufferedBodyBytes) {
        var segment = this.pooled.segment().asSlice(bodyStart, bufferedBodyBytes);
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
        this.pooled = segments.acquire();
        this.bufferSize = pooled.size();
        this.buffer = this.pooled.segment().asByteBuffer();
    }

    private void release() {
        segments.release(this.pooled);
        this.pooled = null;
    }

    private void expand() {
        throw new UnsupportedOperationException("Not yet implemented");
//        if (doubled == 2) {
//            throw new IllegalStateException("Buffer size exhausted: " + bufferSize);
//        }
//        doubled++;
//        var oldSegment = segment;
//        this.segment = segments.acquire(doubled);
//        this.segment.copyFrom(oldSegment);
//        this.buffer = segment.asByteBuffer();
//        this.buffer.position(Math.toIntExact(maskEnd));
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
            pooled.segment(),
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
