package com.github.kjetilv.uplift.asynchttp;

import module java.base;
import com.github.kjetilv.uplift.kernel.io.BytesIO;
import com.github.kjetilv.uplift.util.CaseInsensitiveHashMap;
import com.github.kjetilv.uplift.util.ToStrings;
import com.github.kjetilv.uplift.uuid.Uuid;

@SuppressWarnings("unused")
public record HttpRes(
    int status,
    Map<String, List<String>> headers,
    byte[] body,
    Uuid reqId
) {

    public HttpRes(byte[] body, Uuid reqId) {
        this(0, body, reqId);
    }

    public HttpRes(int status, byte[] body, Uuid reqId) {
        this(status, null, body, reqId);
    }

    public HttpRes(int status, Uuid reqId) {
        this(status, Collections.emptyMap(), NO_BODY, reqId);
    }

    public HttpRes(int status, Map<String, List<String>> headers, byte[] body, Uuid reqId) {
        this.status = httpStatus(status);
        this.headers = CaseInsensitiveHashMap.wrap(headers);
        this.body = BytesIO.nonNull(body);
        this.reqId = reqId;
    }

    public HttpRes updateHeaders(
        Function<? super Map<String, List<String>>, ? extends Map<String, List<String>>> headerModifier
    ) {
        return new HttpRes(status(), headerModifier.apply(headers()), body(), reqId());
    }

    public int size() {
        return headersSize() + body.length;
    }

    @Override
    public String toString() {
        var base = new StringBuilder().append(getClass().getSimpleName())
            .append("[")
            .append(reqId)
            .append(" ")
            .append(status);

        if (!headers.isEmpty()) {
            ToStrings.print(base, headers);
        }
        if (body != null && body.length > 0) {
            ToStrings.print(base, body);
        }
        return base.append("]")
            .toString();
    }

    String toResponseHeader() {
        var sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(status).append(" ").append(readable(status)).append('\n');
        headers().forEach((key, value) -> {
            if (!("Content-Length".equalsIgnoreCase(key) || value.isEmpty())) {
                sb.append(key).append(": ").append(String.join(",", value)).append('\n');
            }
        });
        sb.append("Content-Length: ").append(body.length).append("\n\n");
        return sb.toString();
    }

    boolean hasBody() {
        return body != null && body.length > 0;
    }

    private int headersSize() {
        return headers.entrySet()
            .stream().mapToInt(e -> length(e.getKey(), e.getValue())).sum();
    }

    public static final byte[] NO_BODY = {};

    private static final int OK = 200;

    private static final int INTERNAL_ERROR = 500;

    private static final int OOB = 600;

    private static int httpStatus(int status) {
        return status < OOB
            ? Math.max(status, OK)
            : INTERNAL_ERROR;
    }

    private static boolean isHttpStatus(int status) {
        return OK <= status && status < OOB;
    }

    @SuppressWarnings("MagicNumber")
    private static String readable(int status) {
        return switch (status) {
            case OK -> "OK";
            case 201 -> "Created";
            case 202 -> "Accepted";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case INTERNAL_ERROR -> "Internal Server Error";
            case 501 -> "Not Implemented";
            case 502 -> "Bad Gateway";
            default -> "UNKNOWN";
        };
    }

    private static int length(CharSequence key, List<String> values) {
        return key.length() + 5 + values.stream().mapToInt(String::length).sum() + (values.size() - 1) * 2;
    }
}
