package com.github.kjetilv.uplift.synchttp.write;

import module java.base;

import static java.nio.charset.StandardCharsets.US_ASCII;

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
        Map.entry(100, "Continue"),
        Map.entry(101, "Switching Protocols"),
        Map.entry(200, "OK"),
        Map.entry(201, "Created"),
        Map.entry(202, "Accepted"),
        Map.entry(204, "No Content"),
        Map.entry(206, "Partial Content"),
        Map.entry(301, "Moved Permanently"),
        Map.entry(302, "Found"),
        Map.entry(303, "See Other"),
        Map.entry(304, "Not Modified"),
        Map.entry(307, "Temporary Redirect"),
        Map.entry(308, "Permanent Redirect"),
        Map.entry(400, "Bad Request"),
        Map.entry(401, "Unauthorized"),
        Map.entry(403, "Forbidden"),
        Map.entry(404, "Not Found"),
        Map.entry(405, "Method Not Allowed"),
        Map.entry(408, "Request Timeout"),
        Map.entry(409, "Conflict"),
        Map.entry(410, "Gone"),
        Map.entry(411, "Length Required"),
        Map.entry(413, "Content Too Large"),
        Map.entry(414, "URI Too Long"),
        Map.entry(415, "Unsupported Media Type"),
        Map.entry(416, "Range Not Satisfiable"),
        Map.entry(417, "Expectation Failed"),
        Map.entry(422, "Unprocessable Content"),
        Map.entry(429, "Too Many Requests"),
        Map.entry(431, "Request Header Fields Too Large"),
        Map.entry(500, "Internal Server Error"),
        Map.entry(501, "Not Implemented"),
        Map.entry(502, "Bad Gateway"),
        Map.entry(503, "Service Unavailable"),
        Map.entry(504, "Gateway Timeout"),
        Map.entry(505, "HTTP Version Not Supported")
    );

    private static final byte[][] LINES = IntStream.rangeClosed(MIN, MAX)
        .mapToObj(code ->
            ("HTTP/1.1 " + code + " " + REASONS.getOrDefault(code, "") + "\r\n").getBytes(US_ASCII))
        .toArray(byte[][]::new);
}
