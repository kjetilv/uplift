package com.github.kjetilv.uplift.lambda;

import module java.base;

import com.github.kjetilv.uplift.util.Virtuals;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.util.Objects.requireNonNull;

final class DefaultLamdbdaManaged implements LamdbdaManaged {

    private final String name;

    private final URI lambdaUri;

    private final LambdaClientSettings settings;

    private final LambdaHandler handler;

    private final HttpClient client;

    private final ExecutorService executor;

    DefaultLamdbdaManaged(
        String name,
        URI lambdaUri,
        LambdaClientSettings settings,
        LambdaHandler handler
    ) {
        this.name = name;
        this.lambdaUri = requireNonNull(lambdaUri, "lambdaUri");
        this.settings = requireNonNull(settings, "settings");
        this.handler = requireNonNull(handler, "handler");
        this.executor = Virtuals.executor(name);
        var builder = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1);
        this.client = applyTimeouts(this.settings, builder).build();
    }

    @Override
    public URI lambdaUri() {
        return lambdaUri;
    }

    @Override
    public LambdaLooper looper(String name) {
        Function<HttpRequest, CompletableFuture<HttpResponse<InputStream>>> fetch = request ->
            this.client.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofInputStream(),
                null
            );
        InvocationSource source = new HttpInvocationSource(
            fetch,
            lambdaUri,
            settings.responseTimeout(),
            settings.time()
        );
        InvocationSink sink = new HttpInvocationSink(fetch, settings.time());
        return LambdaLoopers.looper(name, handler, source, sink, settings.time());
    }

    @Override
    public void close() {
        client.close();
    }

    private static HttpClient.Builder applyTimeouts(
        LambdaClientSettings settings, HttpClient.Builder builder
    ) {
        return settings.hasConnectTimeout()
            ? builder.connectTimeout(settings.connectTimeout())
            : builder;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + ": " + settings + "]";
    }
}
