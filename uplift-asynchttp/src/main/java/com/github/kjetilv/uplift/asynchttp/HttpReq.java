package com.github.kjetilv.uplift.asynchttp;

import module java.base;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.kernel.http.QueryParams;
import com.github.kjetilv.uplift.util.CaseInsensitiveHashMap;
import com.github.kjetilv.uplift.util.ToStrings;

import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static com.github.kjetilv.uplift.kernel.io.BytesIO.nonNull;
import static com.github.kjetilv.uplift.util.CaseInsensitiveHashMap.caseInsensitive;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public record HttpReq(
    String method,
    String path,
    Map<String, List<String>> queryParams,
    Map<String, List<String>> headers,
    byte[] body,
    Hash<K128> id
) {

    static HttpReq readRequest(HttpBytes bytes) {
        var reqLine = new String(bytes.req(), UTF_8);
        var methodMark = reqLine.indexOf(' ');
        var method = method(reqLine, methodMark);
        var url = url(reqLine, methodMark);
        var queryParams = queryParams(reqLine, methodMark + 1);
        var headersPart = new String(bytes.headers(), UTF_8);
        var headers = headers(headersPart);
        return new HttpReq(method, url, queryParams, headers, bytes.body(), K128.random());
    }

    public HttpReq(
        String method,
        String path,
        Map<String, List<String>> queryParams,
        Map<String, List<String>> headers,
        byte[] body,
        Hash<K128> id
    ) {
        this.method = requireNonNull(method, "method");
        this.path = requireNonNull(path, "path");
        this.queryParams = CaseInsensitiveHashMap.wrap(queryParams);
        this.headers = CaseInsensitiveHashMap.wrap(headers);
        this.body = nonNull(body);
        this.id = requireNonNull(id, "id");
    }

    public boolean isGet() {
        return is("get");
    }

    public boolean isPost() {
        return is("post");
    }

    public boolean isCORS() {
        return is("options");
    }

    public boolean is(String method) {
        return this.method.equalsIgnoreCase(method);
    }

    public boolean complete() {
        return body == null || body.length >= contentLength();
    }

    @Override
    public String toString() {
        var base = new StringBuilder().append(getClass().getSimpleName())
            .append("[").append(id).append(" ").append(method).append(" ").append(path);
        if (!queryParams.isEmpty()) {
            ToStrings.print(base, queryParams);
        }
        if (!headers.isEmpty()) {
            ToStrings.print(base, headers);
        }
        if (body != null && body.length > 0) {
            ToStrings.print(base, body);
        }
        return base.append("]").toString();
    }

    private Integer contentLength() {
        var value = this.headers().get("Content-Length");
        if (value == null) {
            return 0;
        }
        return value.stream()
            .findFirst()
            .map(Integer::parseInt).orElse(0);
    }

    private static Map<String, List<String>> headers(String headers) {
        return Arrays.stream(headers.split("\n"))
            .map(String::trim)
            .map(HttpReq::headerEntry)
            .collect(caseInsensitive(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map.Entry<String, List<String>> headerEntry(String line) {
        var index = line.indexOf(':');
        return Map.entry(line.substring(0, index).trim(), values(line, index));
    }

    private static List<String> values(String line, int index) {
        return Arrays.stream(line.substring(index + 1).trim().split(","))
            .toList();
    }

    private static String url(String reqLine, int methodMark) {
        var urlStart = methodMark + 1;
        var queryStart = reqLine.indexOf('?', urlStart);
        if (queryStart > 0) {
            return reqLine.substring(urlStart, queryStart);
        }
        var urlEnd = reqLine.indexOf(' ', urlStart);
        if (urlEnd > 0) {
            return reqLine.substring(urlStart, urlEnd);
        }
        return reqLine.substring(urlStart);
    }

    private static String method(String reqLine, int methodMark) {
        return reqLine.substring(0, methodMark);
    }

    private static Map<String, List<String>> queryParams(String reqLine, int urlStart) {
        var queryIndex = reqLine.indexOf('?', urlStart);
        if (queryIndex < 0) {
            return Collections.emptyMap();
        }
        var urlEnd = reqLine.indexOf(' ', urlStart);
        var queryStart = queryIndex + 1;
        String query;
        try {
            query = urlEnd > 0
                ? reqLine.substring(queryStart, urlEnd)
                : reqLine.substring(queryStart);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse, " + queryStart + "/" + urlEnd + ": " + reqLine, e);
        }
        return QueryParams.read(query);
    }
}
