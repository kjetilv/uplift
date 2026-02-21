package com.github.kjetilv.uplift.lambda;

import module java.base;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public record Invocation(
    Instant created,
    HttpRequest request,
    Throwable requestFailure,
    boolean aborted,
    Instant updated,
    String id,
    LambdaPayload payload,
    LambdaResult result,
    HttpRequest completionRequest,
    CompletionStage<HttpResponse<InputStream>> completionStage,
    HttpResponse<InputStream> completionResponse,
    Throwable responseFailure
) {

    public static Invocation none(HttpRequest initRequest, Instant created) {
        return new Invocation(
            created,
            initRequest,
            null,
            false,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    public static Invocation create(String id, HttpRequest initRequest, LambdaPayload payload, Instant created) {
        return new Invocation(
            created,
            initRequest,
            null,
            false,
            null,
            id,
            payload,
            null,
            null,
            null,
            null,
            null
        );
    }

    public static Invocation fatal(Throwable exception, Instant created) {
        return failed(null, created, exception);
    }

    public static Invocation failed(HttpRequest initRequest, Instant created) {
        return failed(initRequest, created, null);
    }

    public static Invocation failed(HttpRequest initRequest, Instant created, Throwable exception) {
        return new Invocation(
            created,
            initRequest,
            exception,
            true,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    public Invocation(
        Instant created,
        HttpRequest request,
        Throwable requestFailure,
        boolean aborted,
        Instant updated,
        String id,
        LambdaPayload payload,
        LambdaResult result,
        HttpRequest completionRequest,
        CompletionStage<HttpResponse<InputStream>> completionStage,
        HttpResponse<InputStream> completionResponse,
        Throwable responseFailure
    ) {
        this.created = Objects.requireNonNull(created, "created");
        this.request = request;
        this.requestFailure = requestFailure;
        this.aborted = aborted;
        this.updated = updated;
        this.id = id;
        this.payload = payload;
        this.result = result;
        this.completionRequest = completionRequest;
        this.completionStage = completionStage;
        this.completionResponse = completionResponse;
        this.responseFailure = responseFailure;
    }

    public Invocation result(
        Supplier<LambdaResult> result,
        Throwable requestFailure,
        Supplier<Instant> time
    ) {
        return empty() ? this : new Invocation(
            created,
            request,
            requestFailure,
            aborted,
            time.get(),
            id,
            payload,
            result.get(),
            completionRequest,
            completionStage,
            completionResponse,
            responseFailure
        );
    }

    Invocation result(Supplier<LambdaResult> result, Supplier<Instant> time) {
        return empty() ? this : new Invocation(
            created,
            request,
            requestFailure,
            aborted,
            time.get(),
            id,
            this.payload,
            result.get(),
            completionRequest,
            completionStage,
            completionResponse,
            responseFailure
        );
    }

    Map<String, Object> toResult() {
        return Optional.ofNullable(result())
            .map(LambdaResult::toMap)
            .orElseGet(Map::of);
    }

    Duration timeTaken() {
        return Duration.between(created, updated);
    }

    Invocation completed(Supplier<HttpRequest> completionRequest, Supplier<Instant> time) {
        return empty() ? this : new Invocation(
            created,
            request,
            requestFailure,
            aborted,
            time.get(),
            id,
            payload,
            result,
            empty() ? null : completionRequest.get(),
            completionStage,
            completionResponse,
            responseFailure
        );
    }

    Invocation completionFuture(
        Supplier<CompletionStage<HttpResponse<InputStream>>> completionStage,
        Supplier<Instant> time
    ) {
        return new Invocation(
            created,
            request,
            requestFailure,
            aborted,
            time.get(),
            id,
            payload,
            result,
            completionRequest,
            empty() ? null : completionStage.get(),
            completionResponse,
            responseFailure
        );
    }

    CompletionStage<Invocation> completedAt(Supplier<Instant> time) {
        return completionStage == null
            ? CompletableFuture.completedFuture(this)
            : completionStage.thenApply(completion ->
                                        new Invocation(
                                            created,
                                            request,
                                            requestFailure,
                                            aborted,
                                            time.get(),
                                            id,
                                            payload,
                                            result,
                                            completionRequest,
                                            completionStage,
                                            completion,
                                            responseFailure
                                        ));
    }

    boolean empty() {
        return id == null || id.isBlank() || payload == null || aborted;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[[" + id + "] " + (
            requestFailure != null ? "requestFailure: " + requestFailure
                : empty() ? "empty"
                    : payload + " / " + result + " => " + (
                        responseFailure != null
                        ? "responseFailure:" + responseFailure
                            : completionResponse
                    )
        ) + "]";
    }
}
