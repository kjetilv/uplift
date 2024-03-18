package com.github.kjetilv.uplift.kernel.io;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record Range(Long start, Long exclusiveEnd, Long length) {

    private static final Logger log = LoggerFactory.getLogger(Range.class);

    public static Optional<Range> read(String value) {
        return Optional.ofNullable(value).stream()
            .map(COMMA::split)
            .flatMap(Arrays::stream)
            .map(String::trim)
            .flatMap(header -> {
                try {
                    if (header.startsWith(BYTES_PREAMBLE)) {
                        String range = header.substring(BYTES_PREAMBLE_LENGTH).trim();
                        int dashIndex = range.indexOf('-');
                        if (dashIndex < 0) {
                            return empty(header);
                        }
                        Long start = dashIndex == 0 ? null : Long.parseLong(range.substring(0, dashIndex));
                        Long
                            exclusiveEnd =
                            range.endsWith("-")
                                ? null
                                : Long.parseLong(range.substring(dashIndex + 1)) + 1;
                        if (start == null || exclusiveEnd == null || start < exclusiveEnd) {
                            return Stream.of(new Range(start, exclusiveEnd));
                        }
                    }
                } catch (Exception e) {
                    return empty(header);
                }
                return empty(header);
            })
            .reduce(Range::combine);
    }

    public Range(Long start, Long exclusiveEnd) {
        this(start, exclusiveEnd, null);
    }

    public Range withLength(long length) {
        return new Range(start, exclusiveEnd, length);
    }

    public String requestHeader() {
        return BYTES_EQ + rangeRequest();
    }

    private String rangeRequest() {
        return start + "-" + (exclusiveEnd - 1);
    }

    private Range combine(Range range) {
        return new Range(
            Math.min(start(), range.start()),
            exclusiveEnd() == null ? range.exclusiveEnd()
                : range.exclusiveEnd() == null ? exclusiveEnd() :
                    Math.max(exclusiveEnd(), range.exclusiveEnd()),
            Math.max(length(), range.length())
        );
    }

    private static final String BYTES_EQ = "bytes=";

    private static final String BYTES_PREAMBLE = "bytes=";

    private static final int BYTES_PREAMBLE_LENGTH = BYTES_PREAMBLE.length();

    private static final Pattern COMMA = Pattern.compile(",");

    private static Stream<Range> empty(String header) {
        log.debug("Invalid range header skipped: {}", header);
        return Stream.empty();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
               "[" +
               (start == null ? "" : Print.bytes(start)) +
               '-' +
               (exclusiveEnd == null ? "" : Print.bytes(exclusiveEnd)) +
               "]";
    }
}
