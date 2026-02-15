package com.github.kjetilv.uplift.synchttp.rere;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public record ReqHeaders(ReqHeader... headers) implements Iterable<ReqHeader> {

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

    public String header(String name) {
        return header(name.toLowerCase(Locale.ROOT).getBytes(UTF_8));
    }

    public String header(MemorySegment name) {
        for (ReqHeader header : headers) {
            if (header.is(name)) {
                return header.value();
            }
        }
        return null;
    }

    public String header(byte[] bytes) {
        for (ReqHeader header : headers) {
            if (header.is(bytes)) {
                return header.value();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return stream()
            .map(ReqHeader::toString)
            .collect(Collectors.joining("\r\n"));
    }
}
