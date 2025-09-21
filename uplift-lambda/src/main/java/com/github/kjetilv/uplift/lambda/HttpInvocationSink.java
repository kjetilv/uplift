package com.github.kjetilv.uplift.lambda;

import module java.base;
import module java.net.http;

import static java.util.Objects.requireNonNull;

final class HttpInvocationSink implements InvocationSink<HttpRequest, HttpResponse<InputStream>> {

    private final Function<? super HttpRequest, ? extends CompletionStage<HttpResponse<InputStream>>> send;

    private final Supplier<Instant> time;

    HttpInvocationSink(
        Function<? super HttpRequest, ? extends CompletionStage<HttpResponse<InputStream>>> send,
        Supplier<Instant> time
    ) {
        this.send = requireNonNull(send, "send");
        this.time = requireNonNull(time, "time");
    }

    @Override
    public Invocation<HttpRequest, HttpResponse<InputStream>> complete(
        Invocation<HttpRequest, HttpResponse<InputStream>> invocation
    ) {
        return invocation.completionFuture(send, time);
    }
}
