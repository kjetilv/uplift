package com.github.kjetilv.uplift.synchttp.rere;

import module java.base;
import com.github.kjetilv.uplift.synchttp.HttpMethod;
import com.github.kjetilv.uplift.synchttp.Utils;

import static com.github.kjetilv.uplift.synchttp.HttpMethod.*;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.nio.charset.StandardCharsets.US_ASCII;

public record ReqLine(
    MemorySegment segment,
    int urlIndex,
    int urlLength,
    int versionIndex,
    int lineBreak,
    QueryParameters queryParameters
) {

    public ReqLine(
        MemorySegment segment,
        int urlIndex,
        int versionIndex,
        int lineBreak
    ) {
        this(
            segment,
            urlIndex,
            versionIndex,
            lineBreak,
            null
        );
    }

    public ReqLine(
        MemorySegment segment,
        int urlIndex,
        int versionIndex,
        int lineBreak,
        QueryParameters queryParameters
    ) {
        this(
            segment,
            urlIndex,
            versionIndex - urlIndex - 1,
            versionIndex,
            lineBreak,
            queryParameters
        );
    }

    public String method() {
        return Utils.string(segment, 0, methodLength());
    }

    public String url() {
        return Utils.string(segment, urlIndex, urlLength());
    }

    public boolean urlPrefixed(String prefix) {
        return Utils.prefixed(prefix, segment, urlIndex);
    }

    public String version() {
        return Utils.string(segment, versionIndex, versionLength());
    }

    public boolean isHttp11() {
        return isVersion(HTTP_11);
    }

    public boolean isHttp10() {
        return isVersion(HTTP_10);
    }

    public ReqLine withQueryParameters() {
        if (this.queryParameters != null) {
            return this;
        }
        var queryParameters = parseQueryParameters();
        var urlLength = queryParameters.startIndex() - urlIndex;
        return new ReqLine(
            segment,
            urlIndex,
            urlLength,
            versionIndex,
            lineBreak,
            queryParameters
        );
    }

    public HttpMethod getMethod() {
        return switch (charAt(0)) {
            case 'G' -> check(3, GET);
            case 'P' -> switch (charAt(1)) {
                case 'O' -> check(4, POST);
                case 'U' -> check(3, PUT);
                case 'A' -> check(5, PATCH);
                default -> throw new IllegalStateException("Not a valid method: " + this);
            };
            case 'H' -> check(4, HEAD);
            case 'O' -> check(7, OPTIONS);
            case 'D' -> check(6, DELETE);
            default -> throw new IllegalStateException("Not a valid method: " + this);
        };
    }

    private QueryParameters parseQueryParameters() {
        return QueryParameters.parse(segment, urlIndex, urlLength());
    }

    private HttpMethod check(int length, HttpMethod method) {
        if (methodLength() == length) {
            return method;
        }
        throw new IllegalStateException("Not a valid method: " + this);
    }

    private boolean isVersion(MemorySegment version) {
        var length = version.byteSize();
        return versionLength() == length &&
               MemorySegment.mismatch(segment, versionIndex, versionIndex + length, version, 0, length) == -1;
    }

    private int charAt(int offset) {
        var ch = (int) segment.get(JAVA_BYTE, offset);
        return ch < 'a' ? ch : ch - DELTA;
    }

    private int methodLength() {
        return urlIndex - 1;
    }

    private int versionLength() {
        return lineBreak - versionIndex - 1;
    }

    private static final int DELTA = 'a' - 'A';

    private static final MemorySegment HTTP_11 =
        MemorySegment.ofArray("HTTP/1.1".getBytes(US_ASCII));

    private static final MemorySegment HTTP_10 =
        MemorySegment.ofArray("HTTP/1.0".getBytes(US_ASCII));

    @Override
    public String toString() {
        try {
            return "%s %s %s".formatted(method(), url(), version());
        } catch (Exception e) {
            return getClass().getSimpleName() + "[" + segment + "]";
        }
    }
}
