package com.github.kjetilv.uplift.lambda;

import module java.base;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.util.Objects.requireNonNull;

final class DefaultLamdbdaManaged implements LamdbdaManaged {

    private final URI lambdaUri;

    private final LambdaClientSettings settings;

    private final LambdaHandler handler;

    private final HttpClient client;

    DefaultLamdbdaManaged(URI lambdaUri, LambdaClientSettings settings, LambdaHandler handler) {
        this.lambdaUri = requireNonNull(lambdaUri, "lambdaUri");
        this.settings = requireNonNull(settings, "settings");
        this.handler = requireNonNull(handler, "handler");
        var builder = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .executor(Executors.newVirtualThreadPerTaskExecutor());
        this.client = applyTimeouts(this.settings, builder).build();
    }

    @Override
    public URI lambdaUri() {
        return lambdaUri;
    }

    @Override
    public LambdaLooper looper(String name) {
        Function<HttpRequest, CompletionStage<HttpResponse<InputStream>>> fetch = request ->
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
        return getClass().getSimpleName() + "[" + settings + "]";
    }
}
