package com.github.kjetilv.uplift.lambda;

import module java.base;
import module java.net.http;
import module uplift.flogs;
import org.slf4j.Logger;

import static java.util.Objects.requireNonNull;

public final class LambdaLooper<Q, R> implements Runnable, Closeable {

    private static final Logger log = LoggerFactory.getLogger(LambdaLooper.class);

    private final InvocationSource<Q, R> source;

    private final LambdaHandler lambdaHandler;

    private final Function<? super Invocation<Q, R>, ? extends Q> responseResolver;

    private final InvocationSink<Q, R> sink;

    private final BiFunction<? super Invocation<Q, R>, ? super Throwable, Boolean> resultLog;

    private final Supplier<Instant> time;

    private final LongAdder initiated = new LongAdder();

    private final LongAdder initiatedfFail = new LongAdder();

    private final LongAdder completedOk = new LongAdder();

    private final LongAdder completedFail = new LongAdder();

    private final Instant startTime;

    private final AtomicReference<Instant> lastTime = new AtomicReference<>(Instant.EPOCH);

    private final AtomicReference<Duration> duration = new AtomicReference<>(Duration.ZERO);

    LambdaLooper(
        InvocationSource<Q, R> source,
        LambdaHandler lambdaHandler,
        Function<? super Invocation<Q, R>, ? extends Q> responseResolver,
        InvocationSink<Q, R> sink,
        BiFunction<? super Invocation<Q, R>, ? super Throwable, Boolean> resultLog,
        Supplier<Instant> time
    ) {
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
        log.info("Loop started");
        try (
            Streamer<Invocation<Q, R>> streamer = openStream();
            Stream<CompletionStage<Invocation<Q, R>>> stream = streamer.open()
        ) {
            stream.map(stage ->
                    stage.thenApply(this::process)
                        .thenApply(invocation ->
                            invocation.complete(responseResolver, time))
                        .thenApply(sink::complete)
                        .thenCompose(qr -> qr.completedAt(time.get()))
                        .whenComplete(this::updateStats)
                        .exceptionally(exception ->
                            Invocation.failed(exception, time.get())))
                .map(CompletionStage::toCompletableFuture)
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

    private Streamer<Invocation<Q, R>> openStream() {
        return new Streamer<>(source::next);
    }

    private Invocation<Q, R> process(Invocation<Q, R> invocation) {
        try {
            return invocation.process(lambdaHandler, time);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to process " + invocation, e);
        } finally {
            initiated.increment();
            lastTime.set(invocation.created());
        }
    }

    private void updateStats(Invocation<Q, R> invocation, Throwable throwable) {
        if (invocation == null || invocation.empty()) {
            initiatedfFail.increment();
            return;
        }
        try {
            Boolean ok = null;
            try {
                ok = resultLog.apply(invocation, throwable);
            } finally {
                (ok != null && ok ? completedOk : completedFail).increment();
            }
            Duration timeSinceLast = Duration.between(lastTime.get(), invocation.created());
            if (shouldlog(timeSinceLast, initiated.longValue())) {
                log.info("{} completed {}", this, invocation);
            }
        } finally {
            duration.accumulateAndGet(invocation.timeTaken(), Duration::plus);
            lastTime.set(invocation.created());
        }
    }

    private void handleOutcome(Invocation<Q, R> qr, Throwable throwable) {
        if (qr.requestFailure() == null && throwable == null) {
            log.debug("Completed: {}", qr, throwable);
            return;
        }
        if (qr.requestFailure() instanceof CompletionException completionException) {
            Throwable combined = combine(throwable, completionException.getCause());
            if (throwable instanceof ConnectException || throwable instanceof HttpConnectTimeoutException) {
                log.warn("Connection failed, pausing {}ms: {}", WAIT_MS, combined.toString());
                sleep(WAIT_MS);
            } else {
                log.warn("Request failed: {}", qr, combined);
            }
        } else {
            log.warn("Request failed: {}", qr, combine(throwable, qr.requestFailure()));
        }
    }

    private static final int WAIT_MS = 100;

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
    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        long count = completedOk.longValue() + completedFail.longValue();
        return "%s[%s@%s: init:%s ok:%s fail:%s avg:%s]".formatted(
            getClass().getSimpleName(),
            lambdaHandler,
            startTime,
            initiated,
            completedOk,
            completedFail,
            count > 0 ? Duration.ofMillis(duration.get().toMillis() / count) : Duration.ZERO
        );
    }
}
