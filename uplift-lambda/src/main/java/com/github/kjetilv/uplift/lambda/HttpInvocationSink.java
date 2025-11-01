package com.github.kjetilv.uplift.lambda;

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

record HttpInvocationSink(
    Function<? super HttpRequest, ? extends CompletionStage<HttpResponse<InputStream>>> send,
    Supplier<Instant> time
) implements InvocationSink<HttpRequest, HttpResponse<InputStream>> {

    HttpInvocationSink(
        Function<? super HttpRequest, ? extends CompletionStage<HttpResponse<InputStream>>> send,
        Supplier<Instant> time
    ) {
        this.send = requireNonNull(send, "send");
        this.time = requireNonNull(time, "time");
    }

    @Override
    public Invocation<HttpRequest, HttpResponse<InputStream>> receive(
        Invocation<HttpRequest, HttpResponse<InputStream>> invocation
    ) {
        var completionStage = send.apply(invocation.completionRequest());
        return invocation.completionFuture(completionStage, time);
    }
}
