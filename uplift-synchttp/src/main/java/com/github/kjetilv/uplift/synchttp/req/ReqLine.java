package com.github.kjetilv.uplift.synchttp.req;

import com.github.kjetilv.uplift.synchttp.HttpMethod;
import com.github.kjetilv.uplift.synchttp.util.Utils;

import java.lang.foreign.MemorySegment;
import java.util.function.Supplier;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

public record ReqLine(
    MemorySegment segment,
    int urlIndex,
    int urlLength,
    int versionIndex,
    int lineBreak,
    Supplier<HttpMethod> methodSupplier,
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
            null,
            null
        );
    }

    public ReqLine(
        MemorySegment segment,
        int urlIndex,
        int versionIndex,
        int lineBreak,
        Supplier<HttpMethod> methodSupplier,
        QueryParameters queryParameters
    ) {
        this(
            segment,
            urlIndex,
            versionIndex - urlIndex - 1,
            versionIndex,
            lineBreak,
            methodSupplier,
            queryParameters
        );
    }

    public ReqLine(
        MemorySegment segment,
        int urlIndex,
        int urlLength,
        int versionIndex,
        int lineBreak,
        Supplier<HttpMethod> methodSupplier,
        QueryParameters queryParameters
    ) {
        this.segment = segment;
        this.urlIndex = urlIndex;
        this.urlLength = urlLength;
        this.versionIndex = versionIndex;
        this.lineBreak = lineBreak;
        this.methodSupplier = methodSupplier == null ? this::parseMethod : methodSupplier;
        this.queryParameters = queryParameters;
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

    public ReqLine withQueryParameters() {
        if (this.queryParameters != null) {
            return this;
        }
        var queryParameters = parseQueryParameters();
        if (queryParameters.isEmpty()) {
            return new ReqLine(
                segment,
                urlIndex,
                urlLength,
                versionIndex,
                lineBreak,
                methodSupplier,
                queryParameters
            );
        }
        var urlLength = queryParameters.startIndex() - urlIndex;
        return new ReqLine(
            segment,
            urlIndex,
            urlLength,
            versionIndex,
            lineBreak,
            methodSupplier,
            queryParameters
        );
    }

    public HttpMethod getMethod() {
        return methodSupplier.get();
    }

    private HttpMethod parseMethod() {
        return switch (charAt(0)) {
            case 'G' -> check(3, HttpMethod.GET);
            case 'P' -> switch (charAt(1)) {
                case 'O' -> check(4, HttpMethod.POST);
                case 'U' -> check(3, HttpMethod.PUT);
                default -> throw new IllegalStateException("Not a valid method: " + this);
            };
            case 'H' -> check(4, HttpMethod.HEAD);
            case 'O' -> check(7, HttpMethod.OPTIONS);
            case 'D' -> check(6, HttpMethod.DELETE);
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

    @Override
    public String toString() {
        return "%s %s %s".formatted(method(), url(), version());
    }
}
