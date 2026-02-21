package com.github.kjetilv.uplift.lambda;

import module java.base;
import com.github.kjetilv.uplift.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpRequest;

final class LambdaLoopers {

    private static final Logger log = LoggerFactory.getLogger(LambdaLoopers.class);

    static LambdaLooper looper(
        String name,
        LambdaHandler handler,
        InvocationSource source,
        InvocationSink sink,
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

    static LambdaLooper looper(
        String name,
        InvocationSource source,
        LambdaHandler handler,
        LambdaLooper.ResponseResolver resolver,
        InvocationSink sink,
        LambdaLooper.ResultLog resultLog,
        Supplier<Instant> time
    ) {
        return new LambdaLooper(
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

    private static LambdaLooper.ResponseResolver toResponsePost() {
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

    private static LambdaLooper.ResultLog resultLog() {
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
                ((Invocation) invocation).completionResponse();
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
