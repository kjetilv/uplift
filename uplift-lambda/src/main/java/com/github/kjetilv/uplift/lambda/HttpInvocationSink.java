package com.github.kjetilv.uplift.lambda;

import module java.base;
import module java.net.http;

import static java.util.Objects.requireNonNull;

record HttpInvocationSink(
    Function<? super HttpRequest, ? extends CompletableFuture<HttpResponse<InputStream>>> send,
    InstantSource time
) implements InvocationSink {

    HttpInvocationSink(
        Function<? super HttpRequest, ? extends CompletableFuture<HttpResponse<InputStream>>> send,
        InstantSource time
    ) {
        this.send = requireNonNull(send, "send");
        this.time = requireNonNull(time, "time");
    }

    @Override
    public Invocation receive(Invocation invocation) {
        return invocation.completionFuture(
            () ->
                send.apply(invocation.completionRequest()),
            time
        );
    }
}
