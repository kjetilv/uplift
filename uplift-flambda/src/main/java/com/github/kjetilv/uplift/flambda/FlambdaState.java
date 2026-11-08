package com.github.kjetilv.uplift.flambda;

import module java.base;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind.K128;
import com.github.kjetilv.uplift.util.Non;
import com.github.kjetilv.uplift.util.RuntimeCloseable;

import static java.util.Objects.requireNonNull;

/// Requests waiting for a Lambda, and Lambda invocations waiting for a response.
///
/// Each request is pending from [#exchange(LambdaReq, Duration)] until it ends, by a response, a timeout,
/// throttling or [#close()]. It is then removed. A response that arrives after the request ended is rejected.
public final class FlambdaState implements RuntimeCloseable {

    private final BlockingQueue<Pending> queue;

    private final Map<Hash<K128>, Pending> pending = new ConcurrentHashMap<>();

    private volatile boolean closed;

    public FlambdaState(int queueLength) {
        this.queue = new ArrayBlockingQueue<>(Non.negativeOrZero(queueLength, "queueLength"));
    }

    /// Queues a request for a Lambda and waits for the outcome.
    ///
    /// @param lambdaReq Request
    /// @param timeout   Max time to wait for a Lambda to take the request and respond
    /// @return Outcome
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Outcome exchange(LambdaReq lambdaReq, Duration timeout) {
        requireNonNull(lambdaReq, "lambdaReq");
        requireNonNull(timeout, "timeout");
        if (closed) {
            return new Outcome.Closed();
        }
        var request = new Pending(lambdaReq);
        pending.put(lambdaReq.id(), request);
        try {
            if (!queue.offer(request)) {
                return new Outcome.Throttled();
            }
            if (closed) {
                request.result.complete(new Outcome.Closed());
            }
            return request.result.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return request.result.complete(new Outcome.TimedOut())
                ? new Outcome.TimedOut()
                : request.result.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            request.result.complete(new Outcome.Closed());
            return new Outcome.Closed();
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to get outcome of " + lambdaReq, e);
        } finally {
            queue.remove(request);
            pending.remove(lambdaReq.id(), request);
        }
    }

    /// Waits for the next request for a Lambda. Requests whose caller has already given up are skipped.
    ///
    /// @return The next request, or empty if closed
    public Optional<Delivery> next() {
        while (!closed) {
            Pending request;
            try {
                request = queue.poll(POLL_SLICE.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
            if (request != null && !request.result.isDone()) {
                var invocationId = UUID.randomUUID().toString();
                request.invocationId = invocationId;
                return Optional.of(new Delivery(request.lambdaReq, invocationId));
            }
        }
        return Optional.empty();
    }

    /// Completes a delivered request.
    ///
    /// @param id           Request id
    /// @param invocationId Invocation id echoed by the Lambda, or null if it did not echo one
    /// @param outcome      Outcome
    /// @return Whether the outcome was accepted
    public Completion complete(Hash<K128> id, String invocationId, Outcome outcome) {
        requireNonNull(id, "id");
        requireNonNull(outcome, "outcome");
        var request = pending.get(id);
        if (request == null || request.invocationId == null) {
            return Completion.UNKNOWN_REQUEST;
        }
        if (invocationId != null && !invocationId.equals(request.invocationId)) {
            return Completion.INVALID_INVOCATION;
        }
        return request.result.complete(outcome)
            ? Completion.ACCEPTED
            : Completion.UNKNOWN_REQUEST;
    }

    /// @return Number of requests not yet ended
    public int pending() {
        return pending.size();
    }

    /// Ends all pending requests with [Outcome.Closed], and makes [#next()] return empty.
    @Override
    public void close() {
        closed = true;
        pending.values()
            .forEach(request ->
                request.result.complete(new Outcome.Closed()));
        queue.clear();
    }

    /// Max time [#next()] waits before it checks whether it is closed
    private static final Duration POLL_SLICE = Duration.ofMillis(250);

    /// A request taken by a Lambda
    ///
    /// @param request      Request
    /// @param invocationId Id of this attempt, see `Lambda-Runtime-Invocation-Id`
    public record Delivery(LambdaReq request, String invocationId) {
    }

    private static final class Pending {

        private final LambdaReq lambdaReq;

        private final CompletableFuture<Outcome> result = new CompletableFuture<>();

        private volatile String invocationId;

        private Pending(LambdaReq lambdaReq) {
            this.lambdaReq = lambdaReq;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[queued:" + queue.size() + " pending:" + pending.size() +
               (closed ? " CLOSED" : "") + "]";
    }

    public enum Completion {
        ACCEPTED,
        UNKNOWN_REQUEST,
        INVALID_INVOCATION
    }
}
