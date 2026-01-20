package com.github.kjetilv.uplift.flambda;

import module java.base;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.lambda.RequestOut;
import com.github.kjetilv.uplift.lambda.RequestOutRW;
import com.github.kjetilv.uplift.lambda.ResponseInRW;
import com.github.kjetilv.uplift.synchttp.HttpCallbackProcessor;
import com.github.kjetilv.uplift.synchttp.req.HttpReq;
import com.github.kjetilv.uplift.synchttp.write.HttpResponseCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.kjetilv.uplift.hash.HashKind.K128;

/// HTTP handler that accepts API requests, exposes them to lambda functions and return their responses.
final class LocalLambdaHandler implements HttpCallbackProcessor.HttpHandler {

    private static final Logger log = LoggerFactory.getLogger(LocalLambdaHandler.class);

    private final BlockingQueue<LambdaReq> lambdaRequestQueue;

    private final SyncPoint<Hash<K128>, LambdaReq> requestsFetched = new SyncPoint<>();

    private final SyncPoint<Hash<K128>, LambdaRes> responsesReceived = new SyncPoint<>();

    private final AtomicLong id = new AtomicLong();

    private final AtomicBoolean closed = new AtomicBoolean();

    private final CorsSettings cors;

    private final FlambdaState flambdaState;

    LocalLambdaHandler(FlambdaSettings settings) {
        Objects.requireNonNull(settings, "settings");
        this.lambdaRequestQueue = new ArrayBlockingQueue<>(settings.queueLength());
        this.cors = settings.cors();
        this.flambdaState = new FlambdaState(settings.queueLength());
    }

    @Override
    public void handle(HttpReq req, HttpResponseCallback callback) {
//        if (req.isGet()) {
//            LambdaReq nextRequest = null;
//            while (nextRequest == null) {
//                try {
//                    nextRequest = lambdaRequestQueue.poll(1, TimeUnit.SECONDS);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    log.warn("Interrupted", e);
//                    callback.just(500);
//                    return;
//                }
//                if (nextRequest == null && closed.get()) {
//                    log.info("Server closed");
//                    callback.just(SERVICE_UNAVAILABLE);
//                    return;
//                }
//            }
//            requestsFetched.put(nextRequest.id(), nextRequest);
//            write(callback, nextRequest);
//        } else if (req.isPost()) {
//            var response = lambdaResponse(req.body());
//            responsesReceived.put(id(req.reqLine().url()), response);
//            callback.just(204);
//        } else if (req.isCors()) {
//            var origin = req.origin();
//            if (origin != null && cors.accepts(origin)) {
//                callback.status(200).headers(
//                        "access-control-allow-origin: %s".formatted(origin),
//                        "access-control-allow-methods: %s".formatted(cors.methodsValue()),
//                        "access-control-allow-headers: %s".formatted(cors.headersValue()),
//                        "access-control-max-age: %s".formatted("86400"),
//                        "access-control-allow-credentials: %s".formatted(cors.credentialsValue()
//                        )
//                    )
//                    .done();
//            } else {
//                callback.just(403);
//            }
//        } else {
//            callback.just(400);
//        }
    }

    public void close() {
        if (closed.compareAndSet(false, true)) {
            log.info("{} closed", this);
        }
    }

    LambdaRes lambdaResponse(LambdaReq request) {
        var id = String.valueOf(this.id.incrementAndGet());
//        var identifiedRequest = request.withId(id);
        try {
//            while (true) {
//                boolean offered;
//                try {
//                    offered = lambdaRequestQueue.offer(identifiedRequest, TIMEOUT.toMillis(), MILLISECONDS);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    throw new IllegalStateException("Interrupted", e);
//                }
//                if (offered) {
//                    break;
//                }
//                log.warn("Failed to put request on queue: {}", identifiedRequest);
//            }
        } catch (Exception e) {
//            throw new IllegalStateException("Failed to accept: " + identifiedRequest, e);
        }
//        var fetched = requestsFetched.get(id);
//        return responsesReceived.get(fetched.id());
        return null;
    }

    private static final int OK = 200;

    private static final Duration TIMEOUT = Duration.ofMinutes(1);

    private static final int SERVICE_UNAVAILABLE = 503;

    private static void write(HttpResponseCallback callback, LambdaReq request) {
        callback.status(OK)
            .headers((Map<String, Object>) null)
            .channel(out -> {
                var jsonWriter = RequestOutRW.INSTANCE.channelWriter(64);
                var requestOut = request.out();
                jsonWriter.write(requestOut, out);
            });
    }

    private static LambdaRes lambdaResponse(ReadableByteChannel response) {
        try {
            var responseIn = ResponseInRW.INSTANCE.channelReader(8192).read(response);
            var digest = responseIn.reqId();
            return new LambdaRes(
                null,
                null
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read response: " + response, e);
        }
    }

    private static byte[] lambdaRequest(RequestOut requestOut) {
        try {
//            return RequestOut.write(requestOut);
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to produce lambda payload from " + requestOut, e);
        }
    }

    private static String id(String uri) {
        var parts = uri.split("/");
        return parts[parts.length - 2];
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               "id@" + id + " r/r:" + requestsFetched.size() + "/" + responsesReceived.size() +
               "]";
    }
}
