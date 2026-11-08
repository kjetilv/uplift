package com.github.kjetilv.uplift.json.io;

import module java.base;

/// Escaping of string content for JSON output.
public final class JsonStrings {

    /// Escapes `"`, `\` and all control characters below U+0020. The common control characters use their
    /// short forms (`\n`, `\t` and so on), the others a six-character unicode escape. Other characters are
    /// written as they are.
    ///
    /// @param value String content, without quotes
    /// @return Escaped content, the same instance if nothing needs escaping
    public static String escape(String value) {
        var first = firstEscape(value);
        if (first == value.length()) {
            return value;
        }
        return escape(value, first, new StringBuilder(value.length() + 16).append(value, 0, first))
            .toString();
    }

    /// Appends escaped content, as [#escape(String)] would return it.
    ///
    /// @param value String content, without quotes
    /// @param target Target
    /// @return The target
    public static StringBuilder escape(String value, StringBuilder target) {
        var first = firstEscape(value);
        target.append(value, 0, first);
        return first == value.length()
            ? target
            : escape(value, first, target);
    }

    private JsonStrings() {
    }

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private static final String[] ASCII = IntStream.range(0, 128)
        .mapToObj(c -> String.valueOf((char) c))
        .toArray(String[]::new);

    /// @param c A character
    /// @return The character as a string, shared for ASCII
    static String string(char c) {
        return c < ASCII.length ? ASCII[c] : String.valueOf(c);
    }

    static boolean needsEscape(char c) {
        return c < 0x20 || c == '"' || c == '\\';
    }

    /// @param c A character that [needs escape][#needsEscape(char)]
    /// @return Its short escape, or null if it needs a unicode escape
    static String shortEscape(char c) {
        return switch (c) {
            case '"' -> "\\\"";
            case '\\' -> "\\\\";
            case '\b' -> "\\b";
            case '\f' -> "\\f";
            case '\n' -> "\\n";
            case '\r' -> "\\r";
            case '\t' -> "\\t";
            default -> null;
        };
    }

    static char hex(int nibble) {
        return HEX[nibble];
    }

    private static int firstEscape(String value) {
        var length = value.length();
        var first = 0;
        while (first < length && !needsEscape(value.charAt(first))) {
            first++;
        }
        return first;
    }

    private static StringBuilder escape(String value, int first, StringBuilder escaped) {
        var length = value.length();
        var start = first;
        for (var i = first; i < length; i++) {
            var c = value.charAt(i);
            if (needsEscape(c)) {
                escaped.append(value, start, i);
                var shortEscape = shortEscape(c);
                if (shortEscape == null) {
                    escaped.append("\\u00").append(HEX[c >> 4]).append(HEX[c & 0xF]);
                } else {
                    escaped.append(shortEscape);
                }
                start = i + 1;
            }
        }
        return escaped.append(value, start, length);
    }
}
