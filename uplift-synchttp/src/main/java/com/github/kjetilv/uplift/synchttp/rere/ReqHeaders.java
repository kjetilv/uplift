package com.github.kjetilv.uplift.synchttp.rere;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public record ReqHeaders(ReqHeader[] headers) implements Iterable<ReqHeader> {

    @Override
    public Iterator<ReqHeader> iterator() {
        return stream().iterator();
    }

    public Stream<ReqHeader> stream() {
        return Arrays.stream(headers);
    }

    public ReqHeader get(int index) {
        return headers[index];
    }

    public String get(String name) {
        var bytes = name.toLowerCase(Locale.ROOT).getBytes(UTF_8);
        for (ReqHeader header : headers) {
            if (header.is(bytes)) {
                return header.value();
            }
        }
        return null;
    }

    public Optional<String> header(String name) {
        return stream().filter(header ->
                header.is(name.toLowerCase(Locale.ROOT).getBytes(UTF_8)))
            .map(ReqHeader::value)
            .flatMap(Stream::ofNullable)
            .findFirst();
    }

    @Override
    public String toString() {
        return stream()
            .map(ReqHeader::toString)
            .collect(Collectors.joining("\r\n"));
    }
}
