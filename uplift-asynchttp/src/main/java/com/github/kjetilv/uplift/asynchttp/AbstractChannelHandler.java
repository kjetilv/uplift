package com.github.kjetilv.uplift.asynchttp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public abstract class AbstractChannelHandler<S extends ChannelState, C extends AbstractChannelHandler<S, C>>
    implements ChannelHandler<S, C> {

    private static final Logger log = LoggerFactory.getLogger(AbstractChannelHandler.class);

    private final AsynchronousByteChannel channel;

    private final int maxRequestLength;

    private final Supplier<Instant> time;

    private final LongAdder bytesRead = new LongAdder();

    protected AbstractChannelHandler(AsynchronousByteChannel channel, int maxRequestLength, Supplier<Instant> time) {
        this.channel = channel;
        this.maxRequestLength = maxRequestLength > 0 ? maxRequestLength : DEFAULT_MAX_REQUEST_LENGTH;
        this.time = requireNonNull(time, "clock");
    }

    @Override
    public final void completed(Integer result, S state) {
        if (bytesRead(result, state)) {
            if (processingCompleted(state)) {
                close();
            } else {
                continueRead(state);
            }
        } else {
            close();
        }
    }

    @Override
    public final void failed(Throwable exc, S state) {
        log.warn("Failed to read: {}", state, exc);
    }

    protected Processing handlePreflight(Object request, boolean post) {
        return writeResponse(
            post
                ? PREFLIGHT_HEADERS_POST
                : PREFLIGHT_HEADERS,
            "Preflight handled: {}",
            request
        );
    }

    protected Processing handleHealth(Object request) {
        return writeResponse(
            HEALTH_HEADERS,
            "Health handled: {}",
            request
        );
    }

    protected final int maxRequestLength() {
        return maxRequestLength;
    }

    protected final Supplier<Instant> clock() {
        return time;
    }

    protected final void close() {
        if (channel == null) {
            return;
        }
        try {
            channel.close();
        } catch (Exception e) {
            log.warn("{}: Failed to close {}", this, channel, e);
        }
    }

    protected final BufferedWriter<ByteBuffer> responseWriter() {
        return new AsyncByteChannelBufferedWriter(channel);
    }

    protected final Instant now() {
        return time.get();
    }

    protected abstract Processing process(S state);

    private boolean bytesRead(Integer result, S state) {
        if (result != null && result >= 0) {
            if (result == 0) {
                log.debug("No bytes read: {}", state);
                return true;
            }
            log.debug("Processing {} bytes...: {}", result, state);
            bytesRead.add(result);
            if (exceedsSize(state)) {
                log.warn("{}: Request too big", this);
                return false;
            }
            return true;
        }
        log.debug("{}: No more request data: {}", this, state);
        return false;
    }

    private boolean processingCompleted(S state) {
        Processing processing = null;
        try {
            processing = process(state);
            return processing != Processing.INCOMPLETE;
        } catch (Exception e) {
            log.warn("Failed something exceptionally: {}", state, e);
            writeResponse(FAILED_HEADERS);
        } finally {
            log(state, processing);
        }
        return true;
    }

    private Processing writeResponse(ByteBuffer headerBuffer, Object... values) {
        try (BufferedWriter<ByteBuffer> writer = responseWriter()) {
            writer.write(new WritableBuffer<>(headerBuffer, headerBuffer.capacity()));
        }
        return Processing.OK;
    }

    private void continueRead(S state) {
        log.debug("{} continuing read: {}", this, state);
        try {
            channel.read(state.requestBuffer(), state, this);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to continue read: " + state, e);
        }
    }

    private void log(S state, Processing processing) {
        if (processing == null) {
            log.error("No processing: {}", state);
        }
        if (processing == Processing.FAIL) {
            log.warn("Failed: {}", state);
        }
        log.debug("{}: {}", processing, state);
    }

    private boolean exceedsSize(S state) {
        return state.requestBuffer().remaining() == 0 || bytesRead.longValue() > maxRequestLength;
    }

    private static final int DEFAULT_MAX_REQUEST_LENGTH = 1024;

    private static final ByteBuffer HEALTH_HEADERS = buffer("""
        HTTP/1.1 200 No Content
        Cache-Control: no-cache
        
        """);

    private static final ByteBuffer FAILED_HEADERS = buffer("""
        HTTP/1.1 500
        Cache-Control: no-cache
        
        """);

    private static final ByteBuffer PREFLIGHT_HEADERS = buffer("""
        HTTP/1.1 204 No Content
        Access-Control-Allow-Origin: *
        Access-Control-Allow-Methods: OPTIONS, GET
        Access-Control-Allow-Headers: Content-Type
        Access-Control-Max-Age: 86400
        Vary: Accept-Encoding, Origin
        Cache-Control: no-cache
        
        """);

    private static final ByteBuffer PREFLIGHT_HEADERS_POST = buffer("""
        HTTP/1.1 204 No Content
        Access-Control-Allow-Origin: *
        Access-Control-Allow-Methods: OPTIONS, GET, POST
        Access-Control-Allow-Headers: Content-Type
        Access-Control-Max-Age: 86400
        Vary: Accept-Encoding, Origin
        Cache-Control: no-cache
        
        """);

    protected static ByteBuffer textBuffer(String text) {
        return ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8));
    }

    private static ByteBuffer buffer(String response) {
        return ByteBuffer.wrap(response.getBytes(StandardCharsets.ISO_8859_1));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + channel + " bytesRead=" + bytesRead + "/" + maxRequestLength + "]";
    }
}
