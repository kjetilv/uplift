package com.github.kjetilv.uplift.synchttp.req;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record ReqHeaders(ReqHeader[] headers) implements Iterable<ReqHeader> {

    public ReqHeader get(int index) {
        return headers[index];
    }

    @Override
    public Iterator<ReqHeader> iterator() {
        return stream().iterator();
    }

    public Stream<ReqHeader> stream() {
        return Arrays.stream(headers);
    }

    @Override
    public String toString() {
        return stream()
            .map(ReqHeader::toString)
            .collect(Collectors.joining("\r\n"));
    }

    public Optional<String> header(String name) {
        return stream().filter(header ->
                header.is(name))
            .map(ReqHeader::value)
            .flatMap(Stream::ofNullable)
            .findFirst();
    }
}
