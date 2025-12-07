package com.github.kjetilv.uplift.asynchttp;

import module java.base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class HttpAsyncChannelHandler extends AbstractAsyncChannelHandler<HttpChannelState, HttpAsyncChannelHandler> {

    private static final Logger log = LoggerFactory.getLogger(HttpAsyncChannelHandler.class);

    private final Server server;

    public HttpAsyncChannelHandler(Server server, int maxRequestLength, Supplier<Instant> time) {
        this(server, maxRequestLength, null, time);
    }

    private HttpAsyncChannelHandler(
        Server server,
        int maxRequestLength,
        AsynchronousByteChannel channel,
        Supplier<Instant> time
    ) {
        super(channel, maxRequestLength, time);
        this.server = requireNonNull(server, "server");
    }

    @Override
    public HttpChannelState channelState(ByteBuffer byteBuffer) {
        return new HttpChannelState(byteBuffer);
    }

    @Override
    public HttpAsyncChannelHandler bind(AsynchronousByteChannel channel) {
        return new HttpAsyncChannelHandler(server, maxRequestLength(), channel, clock());
    }

    @Override
    protected Processing process(HttpChannelState state) {
        var completed = HttpBytes.read(state.requestBuffer())
            .map(HttpReq::readRequest)
            .filter(HttpReq::complete);
        return completed.isEmpty()
            ? Processing.INCOMPLETE
            : completed.map(
                    request ->
                        response(request, server, this::write))
                .map(HttpAsyncChannelHandler::processing)
                .orElse(Processing.FAIL);
    }

    @SuppressWarnings("resource")
    private void write(HttpRes res) {
        var writer = responseWriter();
        var bytes = res.toResponseHeader().getBytes(UTF_8);
        writer.write(new WritableBuffer<>(ByteBuffer.wrap(bytes), bytes.length));
        if (res.hasBody()) {
            writer.write(new WritableBuffer<>(ByteBuffer.wrap(res.body()), res.body().length));
        }
    }

    private static HttpRes response(HttpReq req, Server server, Consumer<? super HttpRes> writer) {
        HttpRes res = null;
        try {
            log.info("Handling {} @ {}", req, server);
            res = server.handle(req);
            log.info("Handled {} -> {} @ {}", req.id(), res, server);
            return res;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to process: " + req, e);
        } finally {
            if (res != null) {
                writer.accept(res);
            }
        }
    }

    @SuppressWarnings("MagicNumber")
    private static Processing processing(HttpRes res) {
        var status = res.status();
        if (200 <= status && status < 400) {
            return Processing.OK;
        }
        if (400 <= status && status < 500) {
            return Processing.REJECTED;
        }
        return Processing.FAIL;
    }

    public interface Server {

        HttpRes handle(HttpReq req);
    }
}
