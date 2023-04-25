package com.github.kjetilv.uplift.asynchttp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.kjetilv.uplift.kernel.io.BytesIO;
import com.github.kjetilv.uplift.kernel.io.CaseInsensitiveHashMap;

import static com.github.kjetilv.uplift.kernel.io.CaseInsensitiveHashMap.caseInsensitive;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public record HttpRequest(
    String method,
    String path,
    Map<String, List<String>> queryParams,
    Map<String, List<String>> headers,
    byte[] body
) {

    static HttpRequest readRequest(HttpBytes bytes) {
        String reqLine = new String(bytes.req(), UTF_8);
        int methodMark = reqLine.indexOf(' ');
        String method = method(reqLine, methodMark);
        String url = url(reqLine, methodMark);
        Map<String, List<String>> queryParams = queryParams(reqLine, methodMark);
        String headersPart = new String(bytes.headers(), UTF_8);
        Map<String, List<String>> headers = headers(headersPart);
        return new HttpRequest(method, url, queryParams, headers, bytes.body());
    }

    public HttpRequest(
        String method,
        String path,
        Map<String, List<String>> queryParams,
        Map<String, List<String>> headers,
        byte[] body
    ) {
        this.method = requireNonNull(method, "method");
        this.path = requireNonNull(path, "path");
        this.queryParams = CaseInsensitiveHashMap.wrap(queryParams);
        this.headers = CaseInsensitiveHashMap.wrap(headers);
        this.body = BytesIO.nonNull(body);
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

    boolean complete() {
        return body == null || body.length >= contentLength();
    }

    private Integer contentLength() {
        return Optional.ofNullable(this.headers().get("Content-Length"))
            .flatMap(values ->
                values.stream().findFirst()).map(Integer::parseInt)
            .orElse(0);
    }

    private static Map<String, List<String>> headers(String headers) {
        return Arrays.stream(headers.split("\n"))
            .map(String::trim)
            .map(HttpRequest::headerEntry)
            .collect(caseInsensitive(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map.Entry<String, List<String>> headerEntry(String line) {
        int index = line.indexOf(':');
        return Map.entry(line.substring(0, index).trim(), values(line, index));
    }

    private static List<String> values(String line, int index) {
        return Arrays.stream(line.substring(index + 1).trim().split(",")).toList();
    }

    private static String url(String reqLine, int methodMark) {
        int urlStart = methodMark + 1;
        int queryStart = reqLine.indexOf('?', urlStart);
        if (queryStart > 0) {
            return reqLine.substring(urlStart, queryStart);
        }
        int urlEnd = reqLine.indexOf(' ', urlStart);
        if (urlEnd > 0) {
            return reqLine.substring(urlStart, urlEnd);
        }
        return reqLine.substring(urlStart);
    }

    private static Map<String, List<String>> queryParams(String reqLine, int methodMark) {
        int urlStart = methodMark + 1;
        int queryIndex = reqLine.indexOf('?', urlStart);
        if (queryIndex < 0) {
            return Collections.emptyMap();
        }
        int urlEnd = reqLine.indexOf(' ', urlStart);
        int queryStart = queryIndex + 1;
        String query = urlEnd > 0 ? reqLine.substring(queryStart, urlEnd) : reqLine.substring(queryStart);
        String[] pairs = query.split("&");
        return Arrays.stream(pairs)
            .map(pair -> pair.split("="))
            .collect(Collectors.groupingBy(
                (String[] pair) ->
                    pair[0].toLowerCase(Locale.ROOT),
                Collectors.toList()
            ))
            .entrySet()
            .stream()
            .collect(caseInsensitive(
                Map.Entry::getKey,
                entry ->
                    entry.getValue().stream().map(pair -> pair[1]).toList()
            ));
    }

    private static String method(String reqLine, int methodMark) {
        return reqLine.substring(0, methodMark);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               method + " " + path + " " + queryParams + " " + headers +
               (body == null || body.length == 0 ? "" : " body: " + body.length) +
               "]";
    }
}
