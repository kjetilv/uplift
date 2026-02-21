package com.github.kjetilv.uplift.lambda;

import module java.base;
import com.github.kjetilv.uplift.util.RuntimeCloseable;
import com.github.kjetilv.uplift.util.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;

import static java.util.Objects.requireNonNull;

public final class LambdaLooper implements Runnable, RuntimeCloseable {

    private static final Logger log = LoggerFactory.getLogger(LambdaLooper.class);

    private final String name;

    private final InvocationSource source;

    private final LambdaHandler lambdaHandler;

    private final ResponseResolver responseResolver;

    private final InvocationSink sink;

    private final ResultLog resultLog;

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
        InvocationSource source,
        LambdaHandler lambdaHandler,
        ResponseResolver responseResolver,
        InvocationSink sink,
        ResultLog resultLog,
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
        try {
            invocationFutures().map(this::run)
                .peek(future ->
                    future.whenComplete(this::handleOutcome))
                .forEach(stage ->
                    stage.toCompletableFuture().join());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to respond", e);
        }
    }

    @Override
    public void close() {
        source.close();
    }

    private Stream<CompletionStage<Invocation>> invocationFutures() {
        return Stream.generate(source::next)
            .takeWhile(Optional::isPresent)
            .flatMap(Optional::stream);
    }

    private CompletionStage<Invocation> run(CompletionStage<Invocation> stage) {
        return stage.thenApply(this::executeLambda)
            .thenApply(this::prepareResponse)
            .thenApply(sink::receive)
            .thenCompose(this::markComplete)
            .whenComplete(this::updateStats)
            .exceptionally(this::fatalInvocation);
    }

    private void handleOutcome(Invocation qr, Throwable throwable) {
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

    private Invocation executeLambda(Invocation invocation) {
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

    private Invocation prepareResponse(Invocation invocation) {
        return invocation.completed(
            () -> responseResolver.resolve(invocation),
            time
        );
    }

    private CompletionStage<Invocation> markComplete(Invocation invocation) {
        return invocation.completedAt(time);
    }

    private void updateStats(Invocation invocation, Throwable throwable) {
        counter(invocation, throwable).increment();
        if (invocation != null) {
            updateTimes(invocation);
        }
    }

    private Invocation fatalInvocation(Throwable exception) {
        return Invocation.fatal(exception, time.get());
    }

    private void updateTimes(Invocation invocation) {
        var timeSinceLast = Duration.between(lastTime.get(), invocation.created());
        if (shouldlog(timeSinceLast, initiated.longValue())) {
            log.info("{} completed {}", this, invocation);
        }
        duration.accumulateAndGet(invocation.timeTaken(), Duration::plus);
        lastTime.set(invocation.created());
    }

    private LongAdder counter(Invocation invocation, Throwable throwable) {
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

    interface ResponseResolver {

        HttpRequest resolve(Invocation invocation);
    }

    interface ResultLog {

        boolean ok(Invocation invocation, Throwable throwable);
    }
}
