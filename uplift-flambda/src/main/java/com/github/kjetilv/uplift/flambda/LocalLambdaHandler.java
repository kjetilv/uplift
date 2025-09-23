package com.github.kjetilv.uplift.flambda;

import module java.base;
import module uplift.asynchttp;
import module uplift.lambda;
import module uplift.util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/// HTTP handler that accepts API requests, exposes them to lambda functions and return their responses.
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
    public HttpRes handle(HttpReq req) {
        if (req.isGet()) {
            return nextRequestResponse();
        }
        if (req.isPost()) {
            return passResponse(req);
        }
        return new HttpRes(400, req.id());
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            log.info("{} closed", this);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id@" + id + "]";
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

    private HttpRes nextRequestResponse() {
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
        Map<String, List<String>> headers = requestHeaders(nextRequest);
        return new HttpRes(OK, headers, body, nextRequest.request().id());
    }

    private HttpRes passResponse(HttpReq req) {
        LambdaResponse response = lambdaResponse(req.body());
        responsesReceived.put(id(req.path()), response);
        return new HttpRes(204, req.id());
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

    private static final HttpRes INTERNAL_ERROR = new HttpRes(500, null);

    private static final HttpRes SERVICE_UNAVAILABLE = new HttpRes(503, null);

    private static Map<String, List<String>> requestHeaders(LambdaRequest nextRequest) {
        return Map.of(
            "Lambda-Runtime-Aws-Request-Id", Collections.singletonList(nextRequest.id()),
            "Content-Type", Collections.singletonList("application/json")
        );
    }

    private static LambdaResponse lambdaResponse(byte[] response) {
        try (ByteArrayInputStream source = new ByteArrayInputStream(response)) {
            ResponseIn responseIn = ResponseIn.read(source);
            return new LambdaResponse(
                responseIn.statusCode(),
                Maps.mapValues(responseIn.headers(), String::valueOf),
                responseIn.body(),
                responseIn.isBase64Encoded(),
                responseIn.reqId()
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read response: " + response.length + " bytes", e);
        }
    }

    private static byte[] lambdaRequest(LambdaRequest request) {
        try {
            return RequestOut.write(request.out());
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to produce lambda payload from " + request, e);
        }
    }

    private static String id(String uri) {
        String[] parts = uri.split("/");
        return parts[parts.length - 2];
    }
}
