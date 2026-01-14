package com.github.kjetilv.uplift.asynchttp.rere;

import module java.base;

public record HttpRequest(RequestLine requestLine, List<RequestHeader> headers, ReadableByteChannel body) {

    @Override
    public String toString() {
        var headers = this.headers.stream()
            .map(RequestHeader::toString)
            .collect(Collectors.joining("\n"));
        return requestLine + "\n" +
               headers + "\n";
    }
}
