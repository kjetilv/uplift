package com.github.kjetilv.uplift.flambda;

import module java.base;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind.K128;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.lambda.RequestOut;
import com.github.kjetilv.uplift.lambda.RequestOutRW;
import com.github.kjetilv.uplift.synchttp.HttpHandler;
import com.github.kjetilv.uplift.synchttp.HttpMethod;
import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import com.github.kjetilv.uplift.synchttp.write.HttpResponseCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

/// The Lambda Runtime API: `/{version}/runtime/invocation/next`, `.../invocation/{id}/response`,
/// `.../invocation/{id}/error` and `.../init/error`. Other paths give 404.
record FlambdaHandler(
    FlambdaSettings settings,
    FlambdaState flambdaState
) implements HttpHandler {

    private static final Logger log = LoggerFactory.getLogger(FlambdaHandler.class);

    @Override
    public void handle(HttpReq httpReq, HttpResponseCallback callback) {
        var segments = Arrays.stream(httpReq.withQueryParameters().path().split("/"))
            .filter(segment -> !segment.isEmpty())
            .toList();
        if (segments.size() < 3 || !segments.get(1).equals("runtime")) {
            error(callback, 404, "NotFound", "Not found: " + httpReq.path());
            return;
        }
        var route = segments.subList(2, segments.size());
        var method = httpReq.method();
        if (route.equals(NEXT)) {
            if (requireMethod(method, HttpMethod.GET, callback)) {
                next(callback);
            }
        } else if (route.equals(INIT_ERROR)) {
            if (requireMethod(method, HttpMethod.POST, callback)) {
                log.error("Lambda failed to initialize: {}", new String(body(httpReq), UTF_8));
                accepted(callback);
            }
        } else if (isInvocation(route, "response")) {
            if (requireMethod(method, HttpMethod.POST, callback)) {
                respond(httpReq, route.get(1), callback);
            }
        } else if (isInvocation(route, "error")) {
            if (requireMethod(method, HttpMethod.POST, callback)) {
                var outcome = LambdaResponses.error(
                    body(httpReq),
                    httpReq.headers().header("lambda-runtime-function-error-type")
                );
                complete(httpReq, route.get(1), outcome, callback);
            }
        } else {
            error(callback, 404, "NotFound", "Not found: " + httpReq.path());
        }
    }

    /// @return True for `invocation/{id}/{action}`
    private static boolean isInvocation(List<String> route, String action) {
        return route.size() == 3 && route.getFirst().equals("invocation") && route.getLast().equals(action);
    }

    private void next(HttpResponseCallback callback) {
        flambdaState.next().ifPresentOrElse(
            delivery -> {
                var request = delivery.request();
                var body = toJson(request.out()).getBytes(UTF_8);
                var deadline = settings.time().instant().plus(settings.timeout()).toEpochMilli();
                callback.status(200)
                    .header("Lambda-Runtime-Aws-Request-Id", request.id().digest())
                    .header("Lambda-Runtime-Deadline-Ms", deadline)
                    .header("Lambda-Runtime-Invoked-Function-Arn", functionArn())
                    .header("Lambda-Runtime-Trace-Id", traceId())
                    .header("Lambda-Runtime-Invocation-Id", delivery.invocationId())
                    .contentType("application/json")
                    .contentLength(body.length)
                    .body(body);
            },
            () -> error(callback, 503, "Runtime.Closed", "Flambda is closed")
        );
    }

    private void respond(HttpReq httpReq, String id, HttpResponseCallback callback) {
        var length = contentLength(httpReq);
        if (length > MAX_RESPONSE_SIZE) {
            var message = "Response exceeds " + MAX_RESPONSE_SIZE + " bytes";
            hash(id).ifPresent(hash ->
                flambdaState.complete(
                    hash,
                    httpReq.headers().header("lambda-runtime-invocation-id"),
                    new Outcome.Malformed(message)
                ));
            error(callback, 413, "RequestEntityTooLarge", message);
            return;
        }
        complete(httpReq, id, LambdaResponses.response(body(httpReq)), callback);
    }

    private void complete(HttpReq httpReq, String id, Outcome outcome, HttpResponseCallback callback) {
        var hash = hash(id);
        if (hash.isEmpty()) {
            error(callback, 400, "InvalidRequestID", "Invalid request ID: " + id);
            return;
        }
        var invocationId = httpReq.headers().header("lambda-runtime-invocation-id");
        switch (flambdaState.complete(hash.get(), invocationId, outcome)) {
            case ACCEPTED -> accepted(callback);
            case UNKNOWN_REQUEST -> error(callback, 400, "InvalidRequestID", "No pending request: " + id);
            case INVALID_INVOCATION -> error(callback, 400, "InvalidInvocationId", "Not the active invocation");
        }
    }

    private String functionArn() {
        return "arn:aws:lambda:local:000000000000:function:" + settings.name();
    }

    private static boolean requireMethod(HttpMethod method, HttpMethod required, HttpResponseCallback callback) {
        if (method == required) {
            return true;
        }
        error(callback, 405, "MethodNotAllowed", method + " not allowed, use " + required);
        return false;
    }

    private static void accepted(HttpResponseCallback callback) {
        json(callback, 202, Map.of("status", "OK"));
    }

    private static void error(HttpResponseCallback callback, int status, String errorType, String errorMessage) {
        json(callback, status, Map.of("errorType", errorType, "errorMessage", errorMessage));
    }

    private static void json(HttpResponseCallback callback, int status, Map<String, Object> map) {
        var bytes = Json.instance().write(map).getBytes(UTF_8);
        callback.status(status)
            .contentType("application/json")
            .contentLength(bytes.length)
            .body(bytes);
    }

    private static Optional<Hash<K128>> hash(String id) {
        try {
            return Optional.of(Hash.from(id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static long contentLength(HttpReq httpReq) {
        var header = httpReq.headers().header("content-length");
        return header == null ? 0L : Long.parseLong(header.strip());
    }

    private static byte[] body(HttpReq httpReq) {
        return httpReq.bodyBytes();
    }

    private static String toJson(RequestOut request) {
        return RequestOutRW.INSTANCE.stringWriter().write(request).toString();
    }

    /// An X-Ray trace header with a new root id, not sampled
    private static String traceId() {
        var random = ThreadLocalRandom.current();
        return "Root=1-%08x-%024x;Parent=%016x;Sampled=0".formatted(
            Instant.now().getEpochSecond(),
            new BigInteger(96, random),
            random.nextLong()
        );
    }

    /// Lambda's limit for synchronous invocation responses
    private static final long MAX_RESPONSE_SIZE = 6L * 1024 * 1024;

    private static final List<String> NEXT = List.of("invocation", "next");

    private static final List<String> INIT_ERROR = List.of("init", "error");

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + settings + " " + flambdaState + "]";
    }
}
