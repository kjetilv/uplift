package com.github.kjetilv.uplift.lambda;

import module java.base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.util.Objects.requireNonNull;

final class HttpInvocationSource implements InvocationSource {

    private static final Logger log = LoggerFactory.getLogger(HttpInvocationSource.class);

    private final URI endpoint;

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
        Supplier<Instant> time
    ) {
        this.fetch = requireNonNull(fetch, "fetch");
        this.endpoint = requireNonNull(endpoint, "api");
        this.time = () -> {
            var instant = time.get();
            return instant == null ? Instant.now() : instant;
        };
        try {
            var builder = HttpRequest.newBuilder().uri(endpoint);
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
    public Optional<CompletionStage<Invocation>> next() {
        if (closed.get()) {
            return Optional.empty();
        }
        var stage = fetch.apply(request)
            .thenApply(response ->
                invocationId(response)
                    .map(invocationId ->
                        invocation(invocationId, response))
                    .orElseGet(this::failedInvocation))
            .exceptionally(this::failedInvocation);
        return Optional.of(stage);
    }

    private Invocation invocation(
        String id,
        HttpResponse<InputStream> response
    ) {
        LambdaPayload payload;
        try {
            payload = LambdaPayload.parse(response.body());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse " + response, e);
        }
        return Invocation.create(id, request, payload, this.time.get());
    }

    private Invocation failedInvocation() {
        return Invocation.failed(request, this.time.get());
    }

    private Invocation failedInvocation(Throwable throwable) {
        try {
            return closed.get()
                ? Invocation.none(request, this.time.get())
                : Invocation.failed(request, this.time.get(), throwable);
        } finally {
            log.warn("Failed to fetch: {}", request, throwable);
        }
    }

    private static Optional<String> invocationId(HttpResponse<? extends InputStream> pollResponse) {
        return pollResponse.headers().allValues("Lambda-Runtime-Aws-Request-Id")
            .stream()
            .filter(Objects::nonNull)
            .filter(value -> !value.isBlank())
            .findFirst();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[@" + endpoint +
               " requests:" + requestsMade +
               (requestsFailed.longValue() > 0 ? " (" + requestsFailed + " failed)" : "") +
               "]";
    }
}
