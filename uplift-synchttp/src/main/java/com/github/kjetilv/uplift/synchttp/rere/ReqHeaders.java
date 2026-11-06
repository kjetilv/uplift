package com.github.kjetilv.uplift.synchttp.rere;

import module java.base;


public record ReqHeaders(ReqHeader... headers) implements Iterable<ReqHeader> {

    @Override
    public Iterator<ReqHeader> iterator() {
        return stream().iterator();
    }

    public Stream<ReqHeader> stream() {
        return Arrays.stream(headers);
    }

    public ReqHeader header(int index) {
        return headers[index];
    }

    /// @param name Name, in ASCII, any case
    /// @return Value of the first header with the name, or null
    public String header(String name) {
        for (var header : headers) {
            if (header.is(name)) {
                return header.value();
            }
        }
        return null;
    }

    public String header(MemorySegment name) {
        for (var header : headers) {
            if (header.is(name)) {
                return header.value();
            }
        }
        return null;
    }

    public String header(byte[] bytes) {
        for (var header : headers) {
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
