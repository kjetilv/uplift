package uplift.flambda;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uplift.asynchttp.HttpChannelHandler;
import uplift.asynchttp.HttpRequest;
import uplift.asynchttp.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * HTTP handler that accepts API requests, exposes them to lambda functions and return their responses.
 */
final class LocalLambdaHandler implements HttpChannelHandler.Server, Closeable {

    private static final Logger log = LoggerFactory.getLogger(LocalLambdaHandler.class);

    private final BlockingQueue<LambdaRequest> lambdaRequestQueue;

    private final Synced<String, LambdaRequest> requestsFetched = new Synced<>(new ConcurrentHashMap<>());

    private final Synced<String, LambdaResponse> responsesReceived = new Synced<>(new ConcurrentHashMap<>());

    private final AtomicLong id = new AtomicLong();

    private final AtomicBoolean closed = new AtomicBoolean();

    LocalLambdaHandler(int queueLength) {
        lambdaRequestQueue = new ArrayBlockingQueue<>(queueLength);
    }

    @Override
    public HttpResponse handle(HttpRequest req) {
        if (req.isGet()) {
            return nextRequestResponse();
        }
        if (req.isPost()) {
            return passResponse(req);
        }
        return BAD_REQUEST;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            log.info("{} closed", this);
        }
    }

    LambdaResponse lambdaResponse(LambdaRequest request) {
        String id = String.valueOf(this.id.incrementAndGet());
        LambdaRequest identifiedRequest = request.withId(id);
        try {
            doPut(identifiedRequest);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to accept: " + identifiedRequest, e);
        }
        LambdaRequest fetched = requestsFetched.get(id);
        return responsesReceived.get(fetched.id());
    }

    private HttpResponse nextRequestResponse() {
        LambdaRequest nextRequest = null;
        while (nextRequest == null) {
            try {
                nextRequest = lambdaRequestQueue.poll(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted", e);
                return INTERNAL_ERROR;
            }
            if (nextRequest == null && closed.get()) {
                log.info("Server closed");
                return SERVICE_UNAVAILABLE;
            }
        }
        requestsFetched.put(nextRequest.id(), nextRequest);
        byte[] body = lambdaRequest(nextRequest);
        return new HttpResponse(OK, requestHeaders(nextRequest), body);
    }

    private HttpResponse passResponse(HttpRequest req) {
        LambdaResponse response = lambdaResponse(req.body());
        responsesReceived.put(id(req.path()), response);
        return NO_CONTENT;
    }

    private void doPut(LambdaRequest request) {
        while (true) {
            boolean offered;
            try {
                offered = lambdaRequestQueue.offer(request, TIMEOUT.toMillis(), MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted", e);
            }
            if (offered) {
                return;
            }
            log.warn("Failed to put request on queue: {}", request);
        }
    }

    private static final int OK = 200;

    private static final Duration TIMEOUT = Duration.ofMinutes(1);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

    private static final HttpResponse INTERNAL_ERROR = new HttpResponse(500);

    private static final HttpResponse SERVICE_UNAVAILABLE = new HttpResponse(503);

    private static final HttpResponse BAD_REQUEST = new HttpResponse(400);

    private static final HttpResponse NO_CONTENT = new HttpResponse(204);

    private static Map<String, List<String>> requestHeaders(LambdaRequest nextRequest) {
        return Map.of(
            "Lambda-Runtime-Aws-Request-Id", Collections.singletonList(nextRequest.id()),
            "Content-Type", Collections.singletonList("application/json")
        );
    }

    private static LambdaResponse lambdaResponse(byte[] response) {
        try {
            return OBJECT_MAPPER.readerFor(LambdaResponse.class).readValue(response);
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to produce lambda response from " + response.length + " bytes", e);
        }
    }

    private static byte[] lambdaRequest(LambdaRequest request) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(request.toJsonPayload());
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to produce lambda payload from " + request, e);
        }
    }

    private static String id(String uri) {
        String[] parts = uri.split("/");
        return parts[parts.length - 2];
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id@" + id + "]";
    }
}
