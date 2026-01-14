package com.github.kjetilv.uplift.flambda;

import module java.base;
import com.github.kjetilv.uplift.asynchttp.HttpAsyncChannelHandler;
import com.github.kjetilv.uplift.asynchttp.HttpReq;
import com.github.kjetilv.uplift.asynchttp.HttpRes;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.lambda.RequestOut;
import com.github.kjetilv.uplift.lambda.ResponseIn;
import com.github.kjetilv.uplift.util.CaseInsensitiveHashMap;
import com.github.kjetilv.uplift.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/// HTTP handler that accepts API requests, exposes them to lambda functions and return their responses.
final class LocalLambdaHandler implements HttpAsyncChannelHandler.Server, Closeable {

    private static final Logger log = LoggerFactory.getLogger(LocalLambdaHandler.class);

    private final BlockingQueue<LambdaRequest> lambdaRequestQueue;

    private final Synced<String, LambdaRequest> requestsFetched = new Synced<>();

    private final Synced<String, LambdaResponse> responsesReceived = new Synced<>();

    private final AtomicLong id = new AtomicLong();

    private final AtomicBoolean closed = new AtomicBoolean();

    private final CorsSettings cors;

    LocalLambdaHandler(LocalLambdaSettings settings) {
        Objects.requireNonNull(settings, "settings");
        this.lambdaRequestQueue = new ArrayBlockingQueue<>(settings.queueLength());
        this.cors = settings.cors();
    }

    @Override
    public HttpRes handle(HttpReq req) {
        if (req.isGet()) {
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
            var body = lambdaRequest(nextRequest);
            var headers = requestHeaders(nextRequest);
            return new HttpRes(
                OK,
                headers,
                body,
                nextRequest.request().id()
            );
        }
        if (req.isPost()) {
            var response = lambdaResponse(req.body());
            responsesReceived.put(id(req.path()), response);
            return new HttpRes(
                204,
                req.id()
            );
        }
        if (req.isCORS()) {
            var origin = req.origin();
            if (cors.accepts(origin)) {
                return new HttpRes(
                    OK,
                    CaseInsensitiveHashMap.wrap(Map.of(
                        "Access-Control-Allow-Origin", List.of(origin),
                        "Access-Control-Allow-Methods", List.of(cors.methodsValue()),
                        "Access-Control-Allow-Headers", List.of(cors.headersValue()),
                        "Access-Control-Max-Age", List.of("86400"),
                        "Access-Control-Allow-Credentials", List.of(cors.credentialsValue())
                    )),
                    req.id()
                );
            }
            return new HttpRes(403, req.id());
        }
        return new HttpRes(400, req.id());
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            log.info("{} closed", this);
        }
    }

    LambdaResponse lambdaResponse(LambdaRequest request) {
        var id = String.valueOf(this.id.incrementAndGet());
        var identifiedRequest = request.withId(id);
        try {
            doPut(identifiedRequest);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to accept: " + identifiedRequest, e);
        }
        var fetched = requestsFetched.get(id);
        return responsesReceived.get(fetched.id());
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
        try (var source = new ByteArrayInputStream(response)) {
            var responseIn = ResponseIn.read(source);
            var digest = responseIn.reqId();
            return new LambdaResponse(
                responseIn.statusCode(),
                Maps.mapValues(responseIn.headers(), String::valueOf),
                responseIn.body(),
                responseIn.isBase64Encoded(),
                digest == null ? null : Hash.from(digest)
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
        var parts = uri.split("/");
        return parts[parts.length - 2];
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               "id@" + id + " r/r:" + requestsFetched.size() + "/" + responsesReceived.size() +
               "]";
    }
}
