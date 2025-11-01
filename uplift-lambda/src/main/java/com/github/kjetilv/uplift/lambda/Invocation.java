package com.github.kjetilv.uplift.lambda;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public record Invocation<Q, R>(
    Instant created,
    Q request,
    Throwable requestFailure,
    boolean aborted,
    Instant updated,
    String id,
    LambdaPayload payload,
    LambdaResult result,
    Q completionRequest,
    CompletionStage<R> completionStage,
    R completionResponse,
    Throwable responseFailure
) {

    public static <Q, R> Invocation<Q, R> none(Q initRequest, Instant created) {
        return new Invocation<>(
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

    public static <Q, R> Invocation<Q, R> create(String id, Q initRequest, LambdaPayload payload, Instant created) {
        return new Invocation<>(
            created,
            initRequest,
            null, false,
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

    public static <Q, R> Invocation<Q, R> fatal(Throwable exception, Instant created) {
        return failed(null, created, exception);
    }

    public static <Q, R> Invocation<Q, R> failed(Q initRequest, Instant created) {
        return failed(initRequest, created, null);
    }

    public static <Q, R> Invocation<Q, R> failed(Q initRequest, Instant created, Throwable exception) {
        return new Invocation<>(
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
        Q request,
        Throwable requestFailure,
        boolean aborted,
        Instant updated,
        String id,
        LambdaPayload payload,
        LambdaResult result,
        Q completionRequest,
        CompletionStage<R> completionStage,
        R completionResponse,
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

    Invocation<Q, R> result(LambdaResult result, Supplier<Instant> time) {
        return empty() ? this : new Invocation<>(
            created,
            request,
            requestFailure,
            aborted,
            time.get(),
            id,
            this.payload,
            result,
            completionRequest,
            completionStage,
            completionResponse,
            responseFailure
        );
    }

    public Invocation<Q, R> result(LambdaResult result, Throwable requestFailure, Supplier<Instant> time) {
        return empty() ? this : new Invocation<>(
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
            completionResponse,
            responseFailure
        );
    }

    Map<String, Object> toResult() {
        return result().toMap();
    }

    Duration timeTaken() {
        return Duration.between(created, updated);
    }

    Invocation<Q, R> completed(Q completionRequest, Supplier<Instant> time) {
        return empty() ? this : new Invocation<>(
            created,
            this.request,
            requestFailure,
            aborted,
            time.get(),
            id,
            payload,
            result,
            completionRequest,
            completionStage,
            completionResponse,
            responseFailure
        );
    }

    Invocation<Q, R> completionFuture(CompletionStage<R> completionStage, Supplier<Instant> time) {
        return empty() ? this : new Invocation<>(
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
            completionResponse,
            responseFailure
        );
    }

    CompletionStage<Invocation<Q, R>> completedAt(Instant time) {
        return empty()
            ? CompletableFuture.completedFuture(this)
            : completionStage.thenApply(completion ->
                new Invocation<>(
                    created,
                    request,
                    requestFailure,
                    aborted,
                    time,
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
