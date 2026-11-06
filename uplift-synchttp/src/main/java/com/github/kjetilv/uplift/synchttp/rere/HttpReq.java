package com.github.kjetilv.uplift.synchttp.rere;

import module java.base;
import com.github.kjetilv.uplift.synchttp.HttpMethod;
import com.github.kjetilv.uplift.util.RuntimeCloseable;

import static com.github.kjetilv.uplift.synchttp.HttpMethod.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/// A request. Its line, headers and body point into the connection's request buffer, which is reused
/// for the next request on the connection. A request, and anything read from it without copying, is
/// only valid until the [handler][com.github.kjetilv.uplift.synchttp.HttpHandler] returns.
@SuppressWarnings("unused")
public record HttpReq(
    ReqLine reqLine,
    ReqHeaders headers,
    ReadableByteChannel body,
    Runnable closer
) implements RuntimeCloseable {

    public static final MemorySegment CONTENT_LENGTH = ms("content-length");

    public ReqHeader header(int index) {
        return headers().header(index);
    }

    public String bodyString() {
        return bodyString(null);
    }

    public String bodyString(Charset charset) {
        return new String(bodyBytes(), charset == null ? UTF_8 : charset);
    }

    /// @return The body. Empty if the request has no body.
    @SuppressWarnings("resource")
    public byte[] bodyBytes() {
        var header = headers.header(CONTENT_LENGTH);
        try {
            return header == null
                ? bodyStream().readAllBytes()
                : bodyStream().readNBytes(Integer.parseInt(header.strip()));
        } catch (Exception e) {
            throw new IllegalStateException("Could not read body", e);
        }
    }

    public InputStream bodyStream() {
        return Channels.newInputStream(body);
    }

    public HttpMethod method() {
        return reqLine.getMethod();
    }

    public int contentLength() {
        var header = headers.header(CONTENT_LENGTH);
        if (header == null || header.isEmpty()) {
            throw new IllegalStateException("No content-length: " + this);
        }
        return Integer.parseInt(header);
    }

    public HttpReq withQueryParameters() {
        return new HttpReq(reqLine.withQueryParameters(), headers, body, closer);
    }

    /// Parses the query parameters. Each call parses again, so keep the result if it is used more than once.
    ///
    /// @return Query parameters
    public QueryParameters queryParameters() {
        return reqLine.withQueryParameters().queryParameters();
    }

    public String path() {
        return reqLine.url();
    }

    public String path(String skip) {
        return reqLine.url().substring(skip.length());
    }

    public boolean isGet() {
        return method() == GET;
    }

    public boolean isPost() {
        return method() == POST;
    }

    public boolean isCors() {
        return method() == OPTIONS;
    }

    public String host() {
        return get(HOST);
    }

    public String origin() {
        return get(ORIGIN);
    }

    /// @return Headers by name. Values of headers with the same name are joined with `,`.
    public Map<String, Object> headerMap() {
        return headers.stream()
            .collect(Collectors.toMap(
                ReqHeader::name,
                ReqHeader::value,
                (value1, value2) -> value1 + "," + value2,
                LinkedHashMap::new
            ));
    }

    @SuppressWarnings("resource")
    public Map<String, Object> queryParametersMap() {
        return withQueryParameters().queryParameters().toMap();
    }

    @Override
    public void close() {
        if (closer != null) {
            closer.run();
        }
    }

    private String get(MemorySegment name) {
        return headers.header(name);
    }

    private static final MemorySegment ORIGIN = ms("origin");

    private static final MemorySegment HOST = ms("host");

    private static MemorySegment ms(String origin) {
        return MemorySegment.ofArray(origin.getBytes(UTF_8));
    }

    @Override
    public String toString() {
        return reqLine + "\r\n" +
               headers + "\r\n";
    }
}
