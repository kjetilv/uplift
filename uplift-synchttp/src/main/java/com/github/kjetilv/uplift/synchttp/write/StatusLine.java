package com.github.kjetilv.uplift.synchttp.write;

import module java.base;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Map.entry;

/// Status lines, with reason phrases, for codes 100 to 599. Built once.
final class StatusLine {

    /// @param statusCode Status code
    /// @return The status line, including CRLF. Do not modify.
    static byte[] bytes(int statusCode) {
        if (statusCode < MIN || statusCode > MAX) {
            throw new IllegalArgumentException("Invalid status code: " + statusCode);
        }
        return LINES[statusCode - MIN];
    }

    private StatusLine() {
    }

    private static final int MIN = 100;

    private static final int MAX = 599;

    private static final Map<Integer, String> REASONS = Map.ofEntries(
        entry(100, "Continue"),
        entry(101, "Switching Protocols"),
        entry(200, "OK"),
        entry(201, "Created"),
        entry(202, "Accepted"),
        entry(204, "No Content"),
        entry(206, "Partial Content"),
        entry(301, "Moved Permanently"),
        entry(302, "Found"),
        entry(303, "See Other"),
        entry(304, "Not Modified"),
        entry(307, "Temporary Redirect"),
        entry(308, "Permanent Redirect"),
        entry(400, "Bad Request"),
        entry(401, "Unauthorized"),
        entry(403, "Forbidden"),
        entry(404, "Not Found"),
        entry(405, "Method Not Allowed"),
        entry(408, "Request Timeout"),
        entry(409, "Conflict"),
        entry(410, "Gone"),
        entry(411, "Length Required"),
        entry(413, "Content Too Large"),
        entry(414, "URI Too Long"),
        entry(415, "Unsupported Media Type"),
        entry(416, "Range Not Satisfiable"),
        entry(417, "Expectation Failed"),
        entry(422, "Unprocessable Content"),
        entry(429, "Too Many Requests"),
        entry(431, "Request Header Fields Too Large"),
        entry(500, "Internal Server Error"),
        entry(501, "Not Implemented"),
        entry(502, "Bad Gateway"),
        entry(503, "Service Unavailable"),
        entry(504, "Gateway Timeout"),
        entry(505, "HTTP Version Not Supported")
    );

    private static final byte[][] LINES = IntStream.rangeClosed(MIN, MAX)
        .mapToObj(code ->
            ("HTTP/1.1 " + code + " " + REASONS.getOrDefault(code, "") + "\r\n").getBytes(US_ASCII))
        .toArray(byte[][]::new);
}
