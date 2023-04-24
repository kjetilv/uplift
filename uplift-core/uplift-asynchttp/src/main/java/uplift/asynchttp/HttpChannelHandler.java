package uplift.asynchttp;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class HttpChannelHandler extends AbstractChannelHandler<HttpChannelState, HttpChannelHandler> {

    private static final Logger log = LoggerFactory.getLogger(HttpChannelHandler.class);

    private final Server server;

    public HttpChannelHandler(Server server, int maxRequestLength, Supplier<Instant> time) {
        this(server, maxRequestLength, null, time);
    }

    private HttpChannelHandler(
        Server server,
        int maxRequestLength,
        AsynchronousByteChannel channel,
        Supplier<Instant> time
    ) {
        super(channel, maxRequestLength, time);
        this.server = requireNonNull(server, "logic");
    }

    @Override
    public HttpChannelState channelState(ByteBuffer byteBuffer) {
        return new HttpChannelState(byteBuffer);
    }

    @Override
    public HttpChannelHandler bind(AsynchronousByteChannel medium) {
        return new HttpChannelHandler(server, maxRequestLength(), medium, clock());
    }

    @Override
    protected Processing process(HttpChannelState state) {
        Optional<HttpRequest> completed =
            HttpBytes.read(state.requestBuffer())
                .map(HttpRequest::readRequest)
                .filter(HttpRequest::complete);
        return completed.isEmpty()
            ? Processing.INCOMPLETE
            : completed.map(request ->
                    response(request, server, this::write))
                .map(HttpChannelHandler::processing)
                .orElse(Processing.FAIL);
    }

    private void write(HttpResponse res) {
        BufferedWriter<ByteBuffer> writer = responseWriter();
        byte[] bytes = res.toResponseHeader().getBytes(UTF_8);
        writer.write(new WritableBuffer<>(ByteBuffer.wrap(bytes), bytes.length));
        if (res.hasBody()) {
            writer.write(new WritableBuffer<>(ByteBuffer.wrap(res.body()), res.body().length));
        }
    }

    private static HttpResponse response(
        HttpRequest req,
        Server server,
        Consumer<? super HttpResponse> writer
    ) {
        HttpResponse res = null;
        try {
            log.info("Handling {}", req);
            res = server.handle(req);
            log.info("Handled {} -> {}", req, res);
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
    private static Processing processing(HttpResponse res) {
        int status = res.status();
        if (200 <= status && status < 400) {
            return Processing.OK;
        }
        if (400 <= status && status < 500) {
            return Processing.REJECTED;
        }
        return Processing.FAIL;
    }

    @FunctionalInterface
    public interface Server {

        HttpResponse handle(HttpRequest req);
    }
}
