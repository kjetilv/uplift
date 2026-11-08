package com.github.kjetilv.uplift.flambda;

import module java.base;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.lambda.ResponseIn;
import com.github.kjetilv.uplift.synchttp.HttpHandler;
import com.github.kjetilv.uplift.synchttp.HttpMethod;
import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import com.github.kjetilv.uplift.synchttp.write.HttpResponseCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

/// The API side, like an API Gateway HTTP API with CORS configured.
///
/// Preflight `OPTIONS` requests are answered from the CORS settings. Other requests become Lambda events, see
/// [Events]. CORS headers from the Lambda are ignored, as API Gateway does when CORS is configured.
///
/// Outcomes that are not a Lambda response give: 502 for a function error or an unusable response, 504 for a
/// timeout, 429 when too many requests are waiting, 503 when Flambda is closed, and 413 for a request larger
/// than the configured limit.
record ApiHandler(
    FlambdaSettings settings,
    FlambdaState flambdaState
) implements HttpHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiHandler.class);

    @Override
    public void handle(HttpReq httpReq, HttpResponseCallback callback) {
        if (httpReq.method() == HttpMethod.OPTIONS) {
            settings.cors().applyTo(httpReq.origin(), callback.status(204)).nobody();
            return;
        }
        var body = body(httpReq);
        if (body.isEmpty()) {
            gatewayError(callback, 413, "Request Entity Too Large");
            return;
        }
        var id = HashKind.K128.random();
        var event = Events.event(httpReq, id.digest(), settings.time().instant(), body.get());
        var lambdaReq = new LambdaReq(id, event);
        var outcome = flambdaState.exchange(lambdaReq, settings.timeout());
        switch (outcome) {
            case Outcome.Response(var in) -> respond(httpReq, in, callback);
            case Outcome.FunctionError _, Outcome.Malformed _ -> gatewayError(callback, 502, "Bad Gateway");
            case Outcome.TimedOut _ -> gatewayError(callback, 504, "Gateway Timeout");
            case Outcome.Throttled _ -> gatewayError(callback, 429, "Too Many Requests");
            case Outcome.Closed _ -> gatewayError(callback, 503, "Service Unavailable");
        }
        logOutcome(lambdaReq, outcome);
    }

    private void respond(HttpReq httpReq, ResponseIn in, HttpResponseCallback callback) {
        var headers = new LinkedHashMap<>(settings.cors().headerMap(httpReq.origin()));
        if (in.headers() != null) {
            in.headers().forEach((name, value) -> {
                var lowerCase = name.toLowerCase(Locale.ROOT);
                if (!lowerCase.startsWith(CORS_PREFIX) && !SERVER_HEADERS.contains(lowerCase)) {
                    headers.put(lowerCase, headerValue(value));
                }
            });
        }
        var response = callback.status(in.statusCode());
        headers.forEach(response::header);
        if (in.cookies() != null) {
            in.cookies().forEach(cookie -> response.header("set-cookie", cookie));
        }
        var bytes = in.bytes();
        if (bytes.length == 0 || noBody(httpReq, in.statusCode())) {
            response.nobody();
        } else {
            response.contentLength(bytes.length).body(bytes);
        }
    }

    /// @return The body, or empty if it is larger than the limit
    private Optional<byte[]> body(HttpReq httpReq) {
        var limit = settings.maxRequestSize();
        var length = httpReq.headers().header("content-length");
        if (length != null && Long.parseLong(length.strip()) > limit) {
            return Optional.empty();
        }
        try {
            var bytes = httpReq.bodyStream().readNBytes(limit + 1);
            return bytes.length > limit ? Optional.empty() : Optional.of(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read body of " + httpReq, e);
        }
    }

    private static boolean noBody(HttpReq httpReq, int status) {
        return httpReq.method() == HttpMethod.HEAD || status < 200 || status == 204 || status == 304;
    }

    private static String headerValue(Object value) {
        return value instanceof Collection<?> values
            ? values.stream().map(String::valueOf).collect(Collectors.joining(","))
            : String.valueOf(value);
    }

    private static void gatewayError(HttpResponseCallback callback, int status, String message) {
        var bytes = Json.instance().write(Map.of("message", message)).getBytes(UTF_8);
        callback.status(status)
            .contentType("application/json")
            .contentLength(bytes.length)
            .body(bytes);
    }

    @SuppressWarnings("LoggingSimilarMessage")
    private static void logOutcome(LambdaReq lambdaReq, Outcome outcome) {
        switch (outcome) {
            case Outcome.Response(var in) when in.status() == ResponseIn.Status.PROCESSING_ERROR ->
                log.error(REQ_FORMAT, ERROR_SYMBOL, lambdaReq, in);
            case Outcome.Response(var in) when in.status() == ResponseIn.Status.BAD_REQUEST ->
                log.warn(REQ_FORMAT, WARN_SYMBOL, lambdaReq, in);
            case Outcome.Response(var in) -> log.info(REQ_FORMAT, OK_SYMBOL, lambdaReq, in);
            default -> log.error(REQ_FORMAT, ERROR_SYMBOL, lambdaReq, outcome);
        }
    }

    private static final String CORS_PREFIX = "access-control-";

    /// Headers the server sets itself
    private static final Set<String> SERVER_HEADERS = Set.of(
        "content-length",
        "transfer-encoding",
        "connection",
        "keep-alive"
    );

    private static final String REQ_FORMAT = "{} {} -> {}";

    private static final String ERROR_SYMBOL = "❌";

    private static final String WARN_SYMBOL = "⚠️";

    private static final String OK_SYMBOL = "✅";

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + settings + " " + flambdaState + "]";
    }
}
