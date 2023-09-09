package com.github.kjetilv.uplift.flambda;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.kjetilv.uplift.asynchttp.HttpChannelHandler;
import com.github.kjetilv.uplift.asynchttp.HttpReq;
import com.github.kjetilv.uplift.asynchttp.HttpRes;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.kernel.uuid.Uuid;
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
        Map<String, Object> map;
        try {
            map = Json.INSTANCE.jsonMap(response);
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to produce lambda response from: " + new String(response, StandardCharsets.UTF_8), e);
        }
        return new LambdaResponse(
            map.getOrDefault("statusCode", 0) instanceof Number statusCode
                ? statusCode.intValue()
                : unexpectedValue("statusCode", map),
            map.getOrDefault("headers", Collections.emptyMap()) instanceof Map<?, ?> headers
                ? check(headers)
                : unexpectedValue("headers", map),
            map.getOrDefault("body", "") instanceof String body
                ? body
                : unexpectedValue("body", map),
            map.getOrDefault("isBase64Encoded", false) instanceof Boolean encoded && encoded,
            map.get("reqId") instanceof String reqId
                ? Uuid.from(reqId)
                : null
        );
    }

    @SuppressWarnings("unchecked")
    private static Map<String, List<String>> check(Map<?, ?> headers) {
        Set<?> keys = headers.keySet();
        if (keys.stream().allMatch(String.class::isInstance)) {
            if (headers.values().stream().allMatch(List.class::isInstance)) {
                return (Map<String, List<String>>) headers;
            }
            return keys.stream().map(String.class::cast)
                .collect(Collectors.toMap(
                    Function.identity(),
                    name ->
                        list(headers, name)
                ));
        }
        throw new IllegalStateException("Invalid header map: " + headers);
    }

    @SuppressWarnings("unchecked")
    private static List<String> list(Map<?, ?> headers, Object key) {
        Object value = headers.get(key);
        String string = value.toString();
        // Lists are unusual, avoid instanceof until we see a possible case
        return string.charAt(0) == '[' && string.endsWith("]") && value instanceof List<?> list
            ? list.stream().map(Object::toString).toList()
            : List.of(string);
    }

    private static <T> T unexpectedValue(String key, Map<String, ?> map) {
        throw new IllegalStateException("Unexpected value: " + key + " -> " + map.get(key));
    }

    private static byte[] lambdaRequest(LambdaRequest request) {
        try {
            return Json.INSTANCE.writeBytes(request.toPayload());
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
