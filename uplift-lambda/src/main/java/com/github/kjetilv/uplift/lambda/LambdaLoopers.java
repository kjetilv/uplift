package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.function.Supplier;

final class LambdaLoopers {

    private static final Logger log = LoggerFactory.getLogger(LambdaLoopers.class);

    static LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper(
        String name,
        LambdaHandler handler,
        InvocationSource<HttpRequest, HttpResponse<InputStream>> source,
        InvocationSink<HttpRequest, HttpResponse<InputStream>> sink,
        Supplier<Instant> time
    ) {
        return looper(
            name,
            source,
            handler,
            toResponsePost(),
            sink,
            resultLog(),
            time
        );
    }

    static <Q, R> LambdaLooper<Q, R> looper(
        String name,
        InvocationSource<Q, R> source,
        LambdaHandler handler,
        LambdaLooper.ResponseResolver<Q, R> resolver,
        InvocationSink<Q, R> sink,
        LambdaLooper.ResultLog<R> resultLog,
        Supplier<Instant> time
    ) {
        return new LambdaLooper<>(
            name,
            source,
            handler,
            resolver,
            sink,
            resultLog,
            time
        );
    }

    private LambdaLoopers() {
    }

    private static LambdaLooper.ResponseResolver<HttpRequest, HttpResponse<InputStream>> toResponsePost() {
        return invocation -> {
            var uri = invocation.request().uri()
                .resolve("/2018-06-01/runtime/invocation/" + invocation.id() + "/response");
            var result = invocation.toResult();
            var jsonResult = Json.instance().write(result);
            return HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(jsonResult))
                .build();
        };
    }

    private static LambdaLooper.ResultLog<HttpResponse<InputStream>> resultLog() {
        return (invocation, throwable) -> {
            if (invocation == null) {
                if (throwable != null) {
                    log.error("Invocation failed", throwable);
                }
                return false;
            }
            if (invocation.empty()) {
                log.warn("Empty invocation, no id resolved");
                return false;
            }
            var completion =
                ((Invocation<?, ? extends HttpResponse<InputStream>>) invocation).completionResponse();
            if (completion == null) {
                return false;
            }
            var statusCode = completion.statusCode();
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
        };
    }
}
