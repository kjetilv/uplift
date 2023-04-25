package com.github.kjetilv.uplift.lambda;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
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
    CompletionStage<R> completionFuture,
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
        return new Invocation<Q, R>(
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

    public static <Q, R> Invocation<Q, R> failed(Throwable exception, Instant created) {
        return failed(null, exception, created);
    }

    public static <Q, R> Invocation<Q, R> failed(Q initRequest, Throwable exception, Instant created) {
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

    Invocation<Q, R> abort() {
        return new Invocation<>(
            created,
            request,
            requestFailure,
            aborted,
            null,
            id,
            payload,
            result,
            completionRequest,
            completionFuture,
            completionResponse,
            responseFailure
        );
    }

    Invocation<Q, R> process(LambdaHandler handler, Supplier<Instant> time) {
        if (isEmpty()) {
            return this;
        }
        LambdaResult result = handler.handle(this.payload());
        return isEmpty() ? this : new Invocation<>(
            created,
            request,
            requestFailure,
            aborted,
            time.get(),
            id,
            this.payload,
            result,
            completionRequest,
            completionFuture,
            completionResponse,
            responseFailure
        );
    }

    Map<String, Object> toResult() {
        LambdaPayload payload = payload();
        return result().toMap(payload != null && payload.isPost());
    }

    Duration timeTaken() {
        return Duration.between(created, updated);
    }

    Invocation<Q, R> complete(
        Function<? super Invocation<Q, R>, ? extends Q> completionRequest,
        Supplier<Instant> time
    ) {
        if (isEmpty()) {
            return this;
        }
        Q completion = completionRequest.apply(this);
        return new Invocation<>(
            created,
            request,
            requestFailure,
            aborted,
            time.get(),
            id,
            payload,
            result,
            completion,
            completionFuture,
            completionResponse,
            responseFailure
        );
    }

    Invocation<Q, R> completionFuture(
        Function<? super Q, ? extends CompletionStage<R>> completer,
        Supplier<Instant> time
    ) {
        if (isEmpty()) {
            return this;
        }
        CompletionStage<R> completionStage =
            completer.apply(completionRequest);
        return new Invocation<>(
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
        if (isEmpty()) {
            return CompletableFuture.completedFuture(this);
        }
        return completionFuture.thenApply(completion ->
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
                completionFuture,
                completion,
                responseFailure
            ));
    }

    boolean isEmpty() {
        return id == null || id.isBlank() || payload == null || aborted;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[[" + id + "] " + (
            requestFailure != null ? "requestFailure: " + requestFailure
                : isEmpty() ? "empty"
                    : payload + " / " + result + " => " + (
                        responseFailure != null
                            ? "responseFailure:" + responseFailure
                            : completionResponse
                    )
        ) + "]";
    }
}
