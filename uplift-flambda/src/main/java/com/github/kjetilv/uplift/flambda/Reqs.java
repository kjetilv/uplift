package com.github.kjetilv.uplift.flambda;

import module java.base;
import module java.net.http;

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
        return execute("GET", headers, null, false);
    }

    default CompletableFuture<HttpResponse<String>> get(URI path, Map<String, String> headers) {
        return path(path).execute("GET", headers, null, false);
    }

    default CompletableFuture<HttpResponse<String>> get(String path, Map<String, String> headers) {
        return path(path).execute("GET", headers, null, false);
    }

    default CompletableFuture<HttpResponse<String>> execute(String method) {
        return execute(method, null, false);
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
