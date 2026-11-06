package com.github.kjetilv.uplift.synchttp.read;

import module java.base;
import com.github.kjetilv.uplift.synchttp.Utils;
import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import com.github.kjetilv.uplift.synchttp.rere.ReqHeader;
import com.github.kjetilv.uplift.synchttp.rere.ReqHeaders;
import com.github.kjetilv.uplift.synchttp.rere.ReqLine;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

/// Reads requests from one connection, one at a time, into one buffer.
///
/// A request, including its headers and body, is valid until the next call to [#read(ReadableByteChannel)].
/// Bytes read past the end of a request are kept, and parsed as the start of the next request.
///
/// The request line and headers must fit in the buffer.
public final class HttpReqReader {

    public static HttpReqReader defaultReader() {
        return new HttpReqReader(Arena.ofAuto().allocate(DEFAULT_SIZE));
    }

    private final MemorySegment segment;

    private final ByteBuffer buffer;

    private final int size;

    /// Start of unparsed data
    private int start;

    /// End of data read from the channel
    private int available;

    private boolean eof;

    /// Body of the last request read
    private BodyBytes body;

    /// Start of the request after the last request read
    private int nextStart;

    public HttpReqReader(MemorySegment segment) {
        this.segment = Objects.requireNonNull(segment, "segment");
        this.buffer = segment.asByteBuffer();
        this.size = Math.toIntExact(segment.byteSize());
    }

    /// @param channel Channel
    /// @return The next request, or null if the channel was closed before a new request started
    /// @throws IllegalStateException If the request could not be read, or if the body of the previous request
    ///                               was not read from the channel, see [#reusable()]
    public HttpReq read(ReadableByteChannel channel) {
        try {
            advance();
            var requestLineFeed = skipEmptyLines(channel);
            if (available == 0) {
                return null;
            }
            var reqLine = requestLine(lineEnd(0, requestLineFeed));
            var headers = new ReqHeaders(headers(channel, requestLineFeed).toArray(ReqHeader[]::new));
            this.body = body(channel, headers);
            return new HttpReq(reqLine, headers, body, null);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read from " + channel, e);
        }
    }

    /// @return True if the next request can be read. This is false if the body of the last request
    ///     was not fully read, and some of it is still unread on the connection.
    public boolean reusable() {
        return body == null || body.unreadFromChannel() == 0;
    }

    /// Reads and discards the unread body of the last request, if no more than `limit` bytes of it
    /// remain on the connection.
    ///
    /// @param limit Max number of bytes to read
    /// @return True if the next request can be read, see [#reusable()]
    public boolean drain(long limit) {
        if (reusable()) {
            return true;
        }
        if (body.unreadFromChannel() > limit) {
            return false;
        }
        body.drain();
        return reusable();
    }

    private void advance() {
        if (body != null) {
            if (!reusable()) {
                throw new IllegalStateException("Body of previous request was not read: " + body);
            }
            start = nextStart;
            body = null;
        }
    }

    /// Skips empty lines before the request line, as recommended by RFC 9112, section 2.2.
    ///
    /// @return Offset of the request line's line feed, or -1 if the channel ended before one was found
    private int skipEmptyLines(ReadableByteChannel channel) {
        while (true) {
            compact();
            var lineFeed = lineFeed(channel, 0);
            if (lineFeed < 0 || lineEnd(0, lineFeed) > 0) {
                return lineFeed;
            }
            start = lineFeed + 1;
        }
    }

    private ReqLine requestLine(int end) {
        var urlSpace = indexOf((byte) ' ', 0, end);
        var versionSpace = urlSpace < 0 ? -1 : indexOf((byte) ' ', urlSpace + 1, end);
        if (versionSpace < 0) {
            throw new IllegalStateException("Malformed request line: " + string(0, end));
        }
        return new ReqLine(segment, urlSpace + 1, versionSpace + 1, end + 1);
    }

    private List<ReqHeader> headers(ReadableByteChannel channel, int requestLineFeed) {
        if (requestLineFeed < 0) {
            nextStart = available;
            return List.of();
        }
        List<ReqHeader> headers = new ArrayList<>();
        var lineStart = requestLineFeed + 1;
        while (true) {
            var lineFeed = lineFeed(channel, lineStart);
            var end = lineEnd(lineStart, lineFeed);
            if (end == lineStart) {
                nextStart = lineFeed < 0 ? available : lineFeed + 1;
                return headers;
            }
            var colon = indexOf((byte) ':', lineStart, end);
            if (colon < 0) {
                throw new IllegalStateException("Malformed header: " + string(lineStart, end));
            }
            headers.add(new ReqHeader(segment, lineStart, colon, end - lineStart));
            if (lineFeed < 0) {
                nextStart = available;
                return headers;
            }
            lineStart = lineFeed + 1;
        }
    }

    private BodyBytes body(ReadableByteChannel channel, ReqHeaders headers) {
        if (headers.header(TRANSFER_ENCODING) != null) {
            throw new UnsupportedOperationException("Transfer-Encoding not supported: " + headers);
        }
        var contentLength = contentLength(headers);
        if (contentLength == 0) {
            return BodyBytes.EMPTY;
        }
        var bodyStart = nextStart;
        var buffered = Math.toIntExact(Math.min(available - bodyStart, contentLength));
        nextStart = bodyStart + buffered;
        return new BodyBytes(
            channel,
            segment.asSlice(bodyStart, buffered).asByteBuffer(),
            contentLength - buffered
        );
    }

    /// Moves unparsed data to the start of the buffer
    private void compact() {
        if (start > 0) {
            var remaining = available - start;
            if (remaining > 0) {
                MemorySegment.copy(segment, start, segment, 0, remaining);
            }
            available = remaining;
            start = 0;
        }
    }

    /// Finds the next line feed, reading more from the channel as needed. Only bytes already read are searched.
    ///
    /// @return Offset of the line feed, or -1 if the channel ended before one was found
    private int lineFeed(ReadableByteChannel channel, int from) {
        var searchFrom = from;
        while (true) {
            var lineFeed = indexOf((byte) '\n', searchFrom, available);
            if (lineFeed >= 0) {
                return lineFeed;
            }
            searchFrom = Math.max(from, available);
            if (!fill(channel)) {
                return -1;
            }
        }
    }

    /// @return End of the line content, excluding any CR before the line feed
    private int lineEnd(int lineStart, int lineFeed) {
        var end = lineFeed < 0 ? available : lineFeed;
        return end > lineStart && segment.get(JAVA_BYTE, end - 1) == '\r' ? end - 1 : end;
    }

    /// @return True if more data was read, false if the channel has ended
    private boolean fill(ReadableByteChannel channel) {
        while (!eof) {
            if (available == size) {
                throw new IllegalStateException("Request line and headers exceed " + size + " bytes");
            }
            buffer.limit(size).position(available);
            int read;
            try {
                read = channel.read(buffer);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read from " + channel, e);
            }
            if (read < 0) {
                eof = true;
            } else if (read > 0) {
                available += read;
                return true;
            }
        }
        return false;
    }

    /// @return Offset of the first `b` in `[from, to)`, or -1
    private int indexOf(byte b, int from, int to) {
        return Utils.indexOf(b, segment, from, to - from);
    }

    private String string(int from, int to) {
        return new String(segment.asSlice(from, to - from).toArray(JAVA_BYTE), StandardCharsets.UTF_8);
    }

    private static final int DEFAULT_SIZE = 1 << 12;

    private static final MemorySegment CONTENT_LENGTH =
        MemorySegment.ofArray("content-length".getBytes(StandardCharsets.UTF_8));

    private static final MemorySegment TRANSFER_ENCODING =
        MemorySegment.ofArray("transfer-encoding".getBytes(StandardCharsets.UTF_8));

    private static long contentLength(ReqHeaders headers) {
        var header = headers.header(CONTENT_LENGTH);
        if (header == null || header.isBlank()) {
            return 0L;
        }
        var contentLength = Long.parseLong(header.strip());
        if (contentLength < 0) {
            throw new IllegalStateException("Negative content-length: " + contentLength);
        }
        return contentLength;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + start + "/" + available + "/" + size + (eof ? " EOF" : "") + "]";
    }
}
