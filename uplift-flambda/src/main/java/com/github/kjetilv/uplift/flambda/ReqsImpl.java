package com.github.kjetilv.uplift.flambda;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
record ReqsImpl(URI uri) implements Reqs {

    @Override
    public Reqs path(URI uri) {
        return new ReqsImpl(this.uri.resolve(uri));
    }

    @Override
    public Reqs path(String uri) {
        return new ReqsImpl(this.uri.resolve(uri));
    }

    @Override
    public CompletableFuture<HttpResponse<String>> execute(String method, String body) {
        return execute(method, body, walksLikeADuck(body.trim()));
    }

    @Override
    public CompletableFuture<HttpResponse<String>> execute(
        String method,
        URI uri,
        Map<String, String> headers,
        String body,
        boolean json
    ) {
        Objects.requireNonNull(method, "method");
        URI uri1 = uri == null ? uri() : uri().resolve(uri);
        try {
            HttpRequest.Builder base = HttpRequest.newBuilder(uri1);
            if (headers != null) {
                headers.forEach(base::header);
            }
            base.method(method, body == null || body.isBlank() ?
                HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));
            if (body != null && json) {
                base.header("Content-Type", "application/json");
            }
            try (HttpClient build = HttpClient.newBuilder().build()) {
                return build.sendAsync(base.build(), HttpResponse.BodyHandlers.ofString());
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean walksLikeADuck(String body) {
        return contained("{", body, "}") || contained("[", body, "]");
    }

    private static boolean contained(String start, String body, String end) {
        return body.startsWith(start) && body.endsWith(end);
    }
}
