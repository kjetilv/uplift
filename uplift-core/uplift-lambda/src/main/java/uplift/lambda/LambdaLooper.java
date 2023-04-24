package uplift.lambda;

import java.io.Closeable;
import java.net.ConnectException;
import java.net.http.HttpConnectTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public final class LambdaLooper<Q, R> implements Runnable, Closeable {

    private static final Logger log = LoggerFactory.getLogger(LambdaLooper.class);

    private final InvocationSource<Q, R> source;

    private final LambdaHandler handler;

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
        LambdaHandler handler,
        Function<? super Invocation<Q, R>, ? extends Q> responseResolver,
        InvocationSink<Q, R> sink,
        BiFunction<? super Invocation<Q, R>, ? super Throwable, Boolean> resultLog,
        Supplier<Instant> time
    ) {
        this.source = requireNonNull(source, "source");
        this.handler = requireNonNull(handler, "handler");
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
            Streamer<CompletionStage<Invocation<Q, R>>> streamer = openStream();
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
                    future.whenComplete(this::handleFailure))
                .forEach(CompletableFuture::join);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to respond", e);
        }
    }

    @Override
    public void close() {
        source.close();
    }

    private Streamer<CompletionStage<Invocation<Q, R>>> openStream() {
        return new Streamer<>(source::next);
    }

    private Invocation<Q, R> process(Invocation<Q, R> invocation) {
        try {
            return invocation.process(handler, time);
        } finally {
            initiated.increment();
            lastTime.set(invocation.created());
        }
    }

    private void updateStats(Invocation<Q, R> invocation, Throwable throwable) {
        if (invocation == null || invocation.isEmpty()) {
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

    private void handleFailure(Invocation<Q, R> qr, Throwable throwable) {
        Throwable failure = qr.requestFailure();
        if (failure == null) {
            log.debug("Completed: {}", qr, throwable);
            return;
        }
        if (failure instanceof CompletionException completionException) {
            Throwable combined = combine(throwable, completionException.getCause());
            if (combined instanceof ConnectException || combined instanceof HttpConnectTimeoutException) {
                log.debug("Connection failed, pausing {}ms: {}", DECENT_WAIT_TIME_MILLIS, combined.toString());
                sleep(DECENT_WAIT_TIME_MILLIS);
            } else {
                log.warn("Request failed: {}", qr, combined);
            }
        } else {
            log.warn("Request failed: {}", qr, combine(throwable, failure));
        }
    }

    private static final int DECENT_WAIT_TIME_MILLIS = 500;

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
            handler,
            startTime,
            initiated,
            completedOk,
            completedFail,
            count > 0 ? Duration.ofMillis(duration.get().toMillis() / count) : Duration.ZERO
        );
    }
}
