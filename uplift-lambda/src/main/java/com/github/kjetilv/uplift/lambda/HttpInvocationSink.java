package com.github.kjetilv.uplift.lambda;

import module java.base;
import module java.net.http;

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
