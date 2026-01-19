package com.github.kjetilv.uplift.asynchttp.rere;

import module java.base;

import static java.nio.charset.StandardCharsets.UTF_8;

public record HttpRequest(RequestLine requestLine, RequestHeader[] headers, ReadableByteChannel body) {

    public HttpRequest(RequestLine requestLine, List<RequestHeader> headers, ReadableByteChannel body) {
        this(requestLine, headers.toArray(RequestHeader[]::new), body);
    }

    public String bodyString() {
        return bodyString(null);
    }

    public String bodyString(Charset charset) {
        try (var reader = Channels.newReader(body, charset == null ? UTF_8 : charset)) {
            return reader.readAllAsString();
        } catch (Exception e) {
            throw new IllegalStateException("Could not read body", e);
        }
    }

    public RequestLine.Method method() {
        return requestLine.getMethod();
    }

    public int contentLength() {
        for (RequestHeader header : headers) {
            if (header.isContentLength()) {
                return Integer.parseInt(header.value());
            }
        }
        throw new IllegalStateException("No content-length: " + this);
    }

    @Override
    public String toString() {
        var headers = Arrays.stream(this.headers)
            .map(RequestHeader::toString)
            .collect(Collectors.joining("\r\n"));
        return requestLine + "\r\n" +
               headers + "\r\n";
    }
}
