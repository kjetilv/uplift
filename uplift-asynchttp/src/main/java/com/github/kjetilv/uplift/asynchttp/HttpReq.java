package com.github.kjetilv.uplift.asynchttp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.kjetilv.uplift.kernel.http.QueryParams;
import com.github.kjetilv.uplift.kernel.io.BytesIO;
import com.github.kjetilv.uplift.kernel.io.CaseInsensitiveHashMap;
import com.github.kjetilv.uplift.kernel.util.ToStrings;
import com.github.kjetilv.uplift.kernel.uuid.Uuid;

import static com.github.kjetilv.uplift.kernel.io.CaseInsensitiveHashMap.caseInsensitive;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public record HttpReq(
    String method,
    String path,
    Map<String, List<String>> queryParams,
    Map<String, List<String>> headers,
    byte[] body,
    Uuid id
) {

    static HttpReq readRequest(HttpBytes bytes) {
        String reqLine = new String(bytes.req(), UTF_8);
        int methodMark = reqLine.indexOf(' ');
        String method = method(reqLine, methodMark);
        String url = url(reqLine, methodMark);
        Map<String, List<String>> queryParams = queryParams(reqLine, methodMark + 1);
        String headersPart = new String(bytes.headers(), UTF_8);
        Map<String, List<String>> headers = headers(headersPart);
        return new HttpReq(method, url, queryParams, headers, bytes.body(), Uuid.random());
    }

    public HttpReq(
        String method,
        String path,
        Map<String, List<String>> queryParams,
        Map<String, List<String>> headers,
        byte[] body,
        Uuid id
    ) {
        this.method = requireNonNull(method, "method");
        this.path = requireNonNull(path, "path");
        this.queryParams = CaseInsensitiveHashMap.wrap(queryParams);
        this.headers = CaseInsensitiveHashMap.wrap(headers);
        this.body = BytesIO.nonNull(body);
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

    private Integer contentLength() {
        List<String> value = this.headers().get("Content-Length");
        if (value == null) {
            return 0;
        }
        return value.stream().findFirst().map(Integer::parseInt).orElse(0);
    }

    private static Map<String, List<String>> headers(String headers) {
        return Arrays.stream(headers.split("\n"))
            .map(String::trim)
            .map(HttpReq::headerEntry)
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

    private static String method(String reqLine, int methodMark) {
        return reqLine.substring(0, methodMark);
    }

    private static Map<String, List<String>> queryParams(String reqLine, int urlStart) {
        int queryIndex = reqLine.indexOf('?', urlStart);
        if (queryIndex < 0) {
            return Collections.emptyMap();
        }
        int urlEnd = reqLine.indexOf(' ', urlStart);
        int queryStart = queryIndex + 1;
        String query = null;
        try {
            query = urlEnd > 0
                ? reqLine.substring(queryStart, urlEnd)
                : reqLine.substring(queryStart);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse, " + queryStart + "/" + urlEnd + ": " + reqLine, e);
        }
        return QueryParams.read(query);
    }

    @Override
    public String toString() {
        StringBuilder base = new StringBuilder().append(getClass().getSimpleName())
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
}
