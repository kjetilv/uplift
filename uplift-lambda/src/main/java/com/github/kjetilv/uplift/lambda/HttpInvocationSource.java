package com.github.kjetilv.uplift.lambda;

import module java.base;
import com.github.kjetilv.uplift.util.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.util.Objects.requireNonNull;

final class HttpInvocationSource implements InvocationSource {

    private static final Logger log = LoggerFactory.getLogger(HttpInvocationSource.class);

    private final URI endpoint;

    private final InstantSource time;

    private final Function<HttpRequest, CompletableFuture<HttpResponse<InputStream>>> fetch;

    private final HttpRequest request;

    private final LongAdder requestsMade = new LongAdder();

    private final LongAdder requestsFailed = new LongAdder();

    private final AtomicBoolean disconnected = new AtomicBoolean();

    private final AtomicBoolean closed = new AtomicBoolean();

    HttpInvocationSource(
        Function<HttpRequest, CompletableFuture<HttpResponse<InputStream>>> fetch,
        URI endpoint,
        Duration timeout,
        InstantSource time
    ) {
        this.fetch = requireNonNull(fetch, "fetch");
        this.endpoint = requireNonNull(endpoint, "api");
        this.time = () -> {
            var instant = time.instant();
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
    public Optional<CompletableFuture<Invocation>> next() {
        if (disconnected.get()) {
            log.info("Trying to reconnect ...");
        }
        if (closed.get()) {
            return Optional.empty();
        }
        var exceptionally =
            fetch.apply(request)
                .thenApply(this::resetDisconnected)
                .thenApply(res ->
                    invocationId(res)
                        .map(id ->
                            toInvocation(id, toPayload(res)))
                        .orElseGet(this::failedInvocation))
                .exceptionally(this::failedInvocation);

        return Optional.of(exceptionally);
    }

    private Invocation toInvocation(String id, LambdaPayload payload) {
        return Invocation.create(id, request, payload, this.time.instant());
    }

    private Invocation failedInvocation() {
        return Invocation.failed(request, this.time.instant());
    }

    private Invocation failedInvocation(Throwable throwable) {
        var closedExceptions = Throwables.chain(throwable)
            .filter(e ->
                e instanceof ConnectException || e instanceof ClosedChannelException)
            .toList();
        if (closedExceptions.isEmpty()) {
            log.warn("Failed to fetch {}", request, throwable);
        } else {
            var printed = closedExceptions.stream()
                .map(Throwable::toString)
                .collect(Collectors.joining(", "));
            log.warn("Failed to fetch {}, connection closed: {}", request, printed);
        }

        if (closed.get()) {
            return Invocation.none(request, this.time.instant());
        }
        disconnected.set(true);
        return Invocation.failed(request, this.time.instant(), throwable);
    }

    private HttpResponse<InputStream> resetDisconnected(HttpResponse<InputStream> response) {
        try {
            return response;
        } finally {
            disconnected.set(false);
        }
    }

    private static LambdaPayload toPayload(HttpResponse<InputStream> response) {
        LambdaPayload payload;
        try {
            payload = LambdaPayload.parse(response.body());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse " + response, e);
        }
        return payload;
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
