package com.github.kjetilv.uplift.lambda;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static java.net.http.HttpClient.Version.HTTP_1_1;
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
        this.client = httpClient(this.settings).build();
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
        InvocationSink sink = new HttpInvocationSink(
            fetch,
            settings.time()
        );
        return LambdaLoopers.looper(name, handler, source, sink, settings.time());
    }

    @Override
    public void close() {
        client.close();
    }

    private static final ExecutorService VIRTUAL_THREADS = Executors.newVirtualThreadPerTaskExecutor();

    private static HttpClient.Builder httpClient(LambdaClientSettings settings) {
        return settings.hasConnectTimeout()
            ? builder().connectTimeout(settings.connectTimeout())
            : builder();
    }

    private static HttpClient.Builder builder() {
        return HttpClient.newBuilder().version(HTTP_1_1)
            .executor(VIRTUAL_THREADS);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + settings + "]";
    }
}
