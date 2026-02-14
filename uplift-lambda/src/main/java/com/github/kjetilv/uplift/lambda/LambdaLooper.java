package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.util.RuntimeCloseable;
import com.github.kjetilv.uplift.util.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.http.HttpConnectTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public final class LambdaLooper<Q, R> implements Runnable, RuntimeCloseable {

    private static final Logger log = LoggerFactory.getLogger(LambdaLooper.class);

    private final String name;

    private final InvocationSource<Q, R> source;

    private final LambdaHandler lambdaHandler;

    private final ResponseResolver<Q, R> responseResolver;

    private final InvocationSink<Q, R> sink;

    private final ResultLog<R> resultLog;

    private final Supplier<Instant> time;

    private final LongAdder initiated = new LongAdder();

    private final LongAdder initiatedFail = new LongAdder();

    private final LongAdder completedOk = new LongAdder();

    private final LongAdder completedFail = new LongAdder();

    private final Instant startTime;

    private final AtomicReference<Instant> lastTime = new AtomicReference<>(Instant.EPOCH);

    private final AtomicReference<Duration> duration = new AtomicReference<>(Duration.ZERO);

    private final AtomicReference<Throwable> lastException = new AtomicReference<>();

    LambdaLooper(
        String name,
        InvocationSource<Q, R> source,
        LambdaHandler lambdaHandler,
        ResponseResolver<Q, R> responseResolver,
        InvocationSink<Q, R> sink,
        ResultLog<R> resultLog,
        Supplier<Instant> time
    ) {
        this.name = requireNonNull(name, "name");
        this.source = requireNonNull(source, "source");
        this.lambdaHandler = requireNonNull(lambdaHandler, "handler");
        this.responseResolver = requireNonNull(responseResolver, "responseResolver");
        this.sink = requireNonNull(sink, "sink");
        this.resultLog = requireNonNull(resultLog, "resultLog");
        this.time = requireNonNull(time, "time");
        this.startTime = this.time.get();
    }

    @Override
    public void run() {
        log.info("{}: Loop started", name);
        try (
            var stages = new Stages<>(source::next);
            var stream = stages.stages()
        ) {
            stream.map(this::toFuture)
                .peek(future ->
                    future.whenComplete(this::handleOutcome))
                .forEach(CompletableFuture::join);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to respond", e);
        }
    }

    @Override
    public void close() {
        source.close();
    }

    private CompletableFuture<Invocation<Q, R>> toFuture(CompletionStage<Invocation<Q, R>> stage) {
        return stage.thenApply(this::executeLambda)
            .thenApply(this::prepareResponse)
            .thenApply(sink::receive)
            .thenCompose(this::markComplete)
            .whenComplete(this::updateStats)
            .exceptionally(this::fatalInvocation)
            .toCompletableFuture();
    }

    private void handleOutcome(Invocation<Q, R> qr, Throwable throwable) {
        if (qr.requestFailure() == null && throwable == null) {
            if (lastException.get() != null) {
                var last = lastException.getAndSet(null);
                log.info("{}: Service restored, last exception: {}", name, last.toString());
            } else {
                log.debug("{}: Completed: {}", name, qr, throwable);
            }
            return;
        }
        if (qr.requestFailure() instanceof CompletionException completionException) {
            var combined = combine(throwable, completionException.getCause());
            lastException.set(combined);
            if (connectOrTimeout(combined)) {
                log.warn("Connection failed, pausing {}ms: {}", WAIT_MS, combined.toString());
                sleep(WAIT_MS);
            } else {
                log.warn("{}: Request failed: {}", name, qr, combined);
            }
        } else {
            var combined = combine(throwable, qr.requestFailure());
            lastException.set(combined);
            log.warn("Request failed: {}", qr, combined);
        }
    }

    private Invocation<Q, R> executeLambda(Invocation<Q, R> invocation) {
        try {
            return invocation.result(
                () ->
                    lambdaHandler.handle(invocation.payload()),
                time
            );
        } catch (Exception e) {
            try {
                return invocation.result(
                    LambdaResult::internalError,
                    e,
                    time
                );
            } finally {
                initiatedFail.increment();
            }
        } finally {
            try {
                lastTime.set(invocation.created());
            } finally {
                initiated.increment();
            }
        }
    }

    private Invocation<Q, R> prepareResponse(Invocation<Q, R> invocation) {
        return invocation.completed(
            () -> responseResolver.resolve(invocation),
            time
        );
    }

    private CompletionStage<Invocation<Q, R>> markComplete(Invocation<Q, R> invocation) {
        return invocation.completedAt(time);
    }

    private void updateStats(Invocation<Q, R> invocation, Throwable throwable) {
        counter(invocation, throwable).increment();
        if (invocation != null) {
            updateTimes(invocation);
        }
    }

    private Invocation<Q, R> fatalInvocation(Throwable exception) {
        return Invocation.fatal(exception, time.get());
    }

    private void updateTimes(Invocation<Q, R> invocation) {
        var timeSinceLast = Duration.between(lastTime.get(), invocation.created());
        if (shouldlog(timeSinceLast, initiated.longValue())) {
            log.info("{} completed {}", this, invocation);
        }
        duration.accumulateAndGet(invocation.timeTaken(), Duration::plus);
        lastTime.set(invocation.created());
    }

    private LongAdder counter(Invocation<Q, R> invocation, Throwable throwable) {
        return resultLog.ok(invocation, throwable)
            ? completedOk
            : completedFail;
    }

    private static final long WAIT_MS = Duration.ofSeconds(1).toMillis();

    private static boolean connectOrTimeout(Throwable throwable) {
        return Stream.of(ConnectException.class, HttpConnectTimeoutException.class)
            .anyMatch(type ->
                Throwables.chain(throwable).anyMatch(type::isInstance));
    }

    private static Throwable combine(Throwable throwable, Throwable failure) {
        if (throwable == null) {
            return failure;
        }
        throwable.addSuppressed(failure);
        return throwable;
    }

    @SuppressWarnings("MagicNumber")
    private static boolean shouldlog(Duration timeSinceLast, long count) {
        return timeSinceLast.getSeconds() > 60 ||
               count <= 5 ||
               count % 5 == 0 && count <= 25 ||
               count % 10 == 0 && count <= 100 ||
               count % 25 == 0;
    }

    @SuppressWarnings("SameParameterValue")
    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        var count = completedOk.longValue() + completedFail.longValue();
        return "%s[%s@%s: init:%s ok:%s fail:%s avg:%s]".formatted(
            name,
            lambdaHandler,
            startTime,
            initiated,
            completedOk,
            completedFail,
            count > 0 ? Duration.ofMillis(duration.get().toMillis() / count) : Duration.ZERO
        );
    }

    interface ResponseResolver<Q, R> {

        Q resolve(Invocation<Q, R> invocation);
    }

    interface ResultLog<R> {

        boolean ok(Invocation<?, R> invocation, Throwable throwable);
    }
}
