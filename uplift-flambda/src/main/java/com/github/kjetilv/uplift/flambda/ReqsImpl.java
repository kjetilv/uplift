package com.github.kjetilv.uplift.flambda;

import module java.base;
import module java.net.http;

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
        try {
            HttpRequest.Builder base = HttpRequest.newBuilder(resolve(uri));
            if (headers != null) {
                headers.forEach(base::header);
            }
            base.method(
                method, body == null || body.isBlank() ?
                    HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(body)
            );
            if (body != null && json) {
                base.header("Content-Type", "application/json");
            }
            try (HttpClient build = HttpClient.newBuilder().build()) {
                return build.sendAsync(base.build(), HttpResponse.BodyHandlers.ofString());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to execute " + method + " " + uri, e);
        }
    }

    private URI resolve(URI uri) {
        return uri == null ? uri() : uri().resolve(uri);
    }

    private static boolean walksLikeADuck(String body) {
        return contained("{", body, "}") || contained("[", body, "]");
    }

    private static boolean contained(String start, String body, String end) {
        return body.startsWith(start) && body.endsWith(end);
    }
}
