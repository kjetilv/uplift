package com.github.kjetilv.uplift.flambda;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public interface Reqs {

    Reqs path(URI uri);

    Reqs path(String uri);

    default CompletableFuture<HttpResponse<String>> get() {
        return execute("GET");
    }

    default CompletableFuture<HttpResponse<String>> get(URI path) {
        return path(path).execute("GET");
    }

    default CompletableFuture<HttpResponse<String>> get(String path) {
        return path(path).execute("GET");
    }

    default CompletableFuture<HttpResponse<String>> get(Map<String, String> headers) {
        return execute("GET", headers);
    }

    default CompletableFuture<HttpResponse<String>> get(URI path, Map<String, String> headers) {
        return path(path).execute("GET", headers);
    }

    default CompletableFuture<HttpResponse<String>> get(String path, Map<String, String> headers) {
        return path(path).execute("GET", headers);
    }

    default CompletableFuture<HttpResponse<String>> execute(String method) {
        return execute(method, false);
    }

    default CompletableFuture<HttpResponse<String>> execute(String method, Map<String, String> headers) {
        return execute(method, headers, null, false);
    }

    default CompletableFuture<HttpResponse<String>> postJson(String body) {
        return post(body, true);
    }

    default CompletableFuture<HttpResponse<String>> post(String body, boolean json) {
        return execute("POST", body, json);
    }

    default CompletableFuture<HttpResponse<String>> execute(String method, String body, boolean json) {
        return execute(method, (URI) null, null, body, json);
    }

    CompletableFuture<HttpResponse<String>> execute(String method, Object body);

    CompletableFuture<HttpResponse<String>> execute(String method, String body);

    default CompletableFuture<HttpResponse<String>> execute(
        String method, Map<String, String> headers, String body, boolean json
    ) {
        return execute(method, (URI) null, headers, body, json);
    }

    default CompletableFuture<HttpResponse<String>> execute(
        String method, String uri, Map<String, String> headers, String body, boolean json
    ) {
        return execute(method, URI.create(uri), headers, body, json);
    }

    CompletableFuture<HttpResponse<String>> execute(
        String method, URI uri, Map<String, String> headers, String body, boolean json
    );
}
