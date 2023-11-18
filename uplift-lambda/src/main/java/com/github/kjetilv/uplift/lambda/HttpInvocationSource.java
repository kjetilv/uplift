package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

final class HttpInvocationSource implements InvocationSource<HttpRequest, HttpResponse<InputStream>> {

    private static final Logger log = LoggerFactory.getLogger(HttpInvocationSource.class);

    private final URI endpoint;

    private final Function<InputStream, Map<?, ?>> jsonParser;

    private final Supplier<Instant> time;

    private final Function<HttpRequest, CompletionStage<HttpResponse<InputStream>>> fetch;

    private final HttpRequest request;

    private final LongAdder requestsMade = new LongAdder();

    private final LongAdder requestsFailed = new LongAdder();

    private final AtomicBoolean closed = new AtomicBoolean();

    HttpInvocationSource(
        Function<HttpRequest, CompletionStage<HttpResponse<InputStream>>> fetch,
        URI endpoint,
        Duration timeout,
        Function<InputStream, Map<?, ?>> jsonParser,
        Supplier<Instant> time
    ) {
        this.fetch = requireNonNull(fetch, "send");
        this.endpoint = requireNonNull(endpoint, "api");
        this.jsonParser = requireNonNull(jsonParser, "jsonParser");
        this.time = () -> {
            Instant instant = time.get();
            return instant == null ? Instant.now() : instant;
        };
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder().uri(endpoint);
            if (timeout.compareTo(Duration.ZERO) > 0) {
                builder.timeout(timeout);
            }
            this.request = builder.build();
        } catch (Exception e) {
            requestsFailed.increment();
            throw new IllegalStateException("Invalid URI: " + endpoint, e);
        } finally {
            requestsMade.increment();
        }
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            log.debug("{} closed", this);
        }
    }

    @Override
    public Optional<CompletionStage<Invocation<HttpRequest, HttpResponse<InputStream>>>> next() {
        if (closed.get()) {
            return Optional.empty();
        }
        return Optional.of(fetch.apply(request)
            .thenApply(response ->
                invocationId(response).map(id ->
                        invocation(response, id))
                    .orElseGet(this::failed))
            .exceptionally(throwable ->
                closed.get()
                    ? Invocation.none(request, time.get())
                    : Invocation.failed(request, throwable, time.get())));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[@" + endpoint + ", requests:" + requestsMade +
            (requestsFailed.longValue() > 0 ? "(" + requestsFailed + " failed)" : "") +
            "]";
    }

    private Invocation<HttpRequest, HttpResponse<InputStream>> invocation(
        HttpResponse<? extends InputStream> response,
        String id
    ) {
        return invocation(id, request, response);
    }

    private Invocation<HttpRequest, HttpResponse<InputStream>> failed() {
        return Invocation.failed(request, null, time.get());
    }

    private Invocation<HttpRequest, HttpResponse<InputStream>> invocation(
        String id,
        HttpRequest request,
        HttpResponse<? extends InputStream> response
    ) {
        LambdaPayload payload = payload(response);
        return Invocation.create(id, request, payload, time.get());
    }

    private LambdaPayload payload(HttpResponse<? extends InputStream> response) {
        InputStream body = validBody(response);
        Map<?, ?> object = object(body);
        return payload(object);
    }

    private Map<?, ?> object(InputStream body) {
        try {
            return parsedBody(body);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to process object", e);
        }
    }

    private Map<?, ?> parsedBody(InputStream body) {
        try {
            return jsonParser.apply(body);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse body contents: " + body, e);
        }
    }

    private static InputStream validBody(HttpResponse<? extends InputStream> response) {
        InputStream body = response.body();
        if (body != null) {
            return body;
        }
        throw new IllegalArgumentException("No body in request: " + response);
    }

    private static LambdaPayload payload(Map<?, ?> input) {
        try {
            return LambdaPayload.create(input);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build payload: " + toEmergencyJsonString(input), e);
        }
    }

    private static Optional<String> invocationId(HttpResponse<? extends InputStream> pollResponse) {
        return pollResponse.headers().allValues("Lambda-Runtime-Aws-Request-Id")
            .stream()
            .filter(Objects::nonNull)
            .filter(value -> !value.isBlank())
            .findFirst();
    }

    private static String toEmergencyJsonString(Map<?, ?> input) {
        try {
            return Json.INSTANCE.write(input);
        } catch (Exception ex) {
            return String.valueOf(input);
        }
    }
}
