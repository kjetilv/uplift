package com.github.kjetilv.uplift.synchttp.rere;

import module java.base;
import com.github.kjetilv.uplift.synchttp.HttpMethod;

import static com.github.kjetilv.uplift.synchttp.HttpMethod.*;
import static java.nio.charset.StandardCharsets.UTF_8;

@SuppressWarnings("unused")
public record HttpReq(ReqLine reqLine, ReqHeaders headers, ReadableByteChannel body) {

    public HttpReq(ReqLine reqLine, List<ReqHeader> headers, ReadableByteChannel body) {
        this(reqLine, new ReqHeaders(headers.toArray(ReqHeader[]::new)), body);
    }

    public ReqHeader header(int index) {
        return headers().get(index);
    }

    public String bodyString() {
        return bodyString(null);
    }

    public String bodyString(Charset charset) {
        return new String(bodyBytes(), charset == null ? UTF_8 : charset);
    }

    @SuppressWarnings("resource")
    public byte[] bodyBytes() {
        try {
            var stream = bodyStream();
            var length = contentLength();
            return stream.readNBytes(length);
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
        for (ReqHeader header : headers) {
            if (header.isContentLength()) {
                return Integer.parseInt(header.value());
            }
        }
        throw new IllegalStateException("No content-length: " + this);
    }

    public HttpReq withQueryParameters() {
        return new HttpReq(reqLine.withQueryParameters(), headers, body);
    }

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
        return get("host");
    }

    public String origin() {
        return get("origin");
    }

    private String get(String name) {
        for (ReqHeader header : headers) {
            if (header.is(name)) {
                return header.value();
            }
        }
        return null;
    }

    public Map<String, Object> headerMap() {
        return headers.stream()
            .map(header ->
                Map.entry(header.name(), header.value()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
    }

    public Map<String, Object> queryParametersMap() {
        return withQueryParameters().queryParameters().toMap();
    }

    @Override
    public String toString() {
        return reqLine + "\r\n" +
               headers + "\r\n";
    }
}
