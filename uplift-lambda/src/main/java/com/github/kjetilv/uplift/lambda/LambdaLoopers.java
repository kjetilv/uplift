package com.github.kjetilv.uplift.lambda;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.kjetilv.uplift.json.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class LambdaLoopers {

    private static final Logger log = LoggerFactory.getLogger(LambdaLoopers.class);

    static LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper(
        LambdaHandler handler,
        InvocationSource<HttpRequest, HttpResponse<InputStream>> source,
        InvocationSink<HttpRequest, HttpResponse<InputStream>> sink,
        Supplier<Instant> time
    ) {
        return looper(
            source,
            handler,
            LambdaLoopers::toLambdaResponsePost,
            sink,
            LambdaLoopers::resultLog,
            time
        );
    }

    static <Q, R> LambdaLooper<Q, R> looper(
        InvocationSource<Q, R> invocationsProvider,
        LambdaHandler handler,
        Function<? super Invocation<Q, R>, ? extends Q> responseResolver,
        InvocationSink<Q, R> completeInvocation,
        BiFunction<Invocation<Q, R>, ? super Throwable, Boolean> resultLog,
        Supplier<Instant> time
    ) {
        return new LambdaLooper<>(
            invocationsProvider,
            handler,
            responseResolver,
            completeInvocation,
            resultLog,
            time
        );
    }

    private LambdaLoopers() {
    }

    @SuppressWarnings("MagicNumber")
    private static boolean resultLog(
        Invocation<HttpRequest, ? extends HttpResponse<InputStream>> invocation,
        Throwable throwable
    ) {
        if (invocation != null) {
            if (invocation.empty()) {
                log.warn("Empty invocation, no id resolved");
                return false;
            }
            HttpResponse<InputStream> completion = invocation.completionResponse();
            if (completion == null) {
                return false;
            }
            int statusCode = completion.statusCode();
            if (statusCode >= 500) {
                log.error("Failed: {} ", invocation);
                return false;
            }
            if (statusCode >= 400) {
                log.error("Not accepted: {} ", invocation);
                return false;
            }
            if (statusCode >= 300) {
                log.error("Redirected: {} ", invocation);
                return true;
            }
            return true;
        }
        if (throwable != null) {
            log.error("Invocation failed", throwable);
        }
        return false;
    }

    private static HttpRequest toLambdaResponsePost(
        Invocation<? extends HttpRequest, HttpResponse<InputStream>> invocation
    ) {
        String jsonResult = IO.JSON.write(invocation.toResult());
        return HttpRequest.newBuilder()
            .uri(responseRequestUri(invocation.request().uri(), invocation.id()))
            .POST(HttpRequest.BodyPublishers.ofString(jsonResult))
            .build();
    }

    private static URI responseRequestUri(URI base, String id) {
        return base.resolve("/2018-06-01/runtime/invocation/" + id + "/response");
    }
}
