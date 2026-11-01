package com.github.kjetilv.uplift.s3.util;

import module java.base;

/// Minimal XML scanning for the fixed-format responses from S3.
///
/// Elements are found by plain open and close tags, without attributes. This holds for the
/// S3 response elements read here. The scanner does not handle nested elements with the same name.
public final class Xml {

    /// @param xml XML text
    /// @param tag Element name
    /// @return Raw content of each `<tag>...</tag>` element, in document order
    public static Stream<String> elements(String xml, String tag) {
        var open = "<" + tag + ">";
        var close = "</" + tag + ">";
        return Stream.iterate(
                find(xml, open, close, 0),
                Objects::nonNull,
                hit -> find(xml, open, close, hit.next())
            )
            .map(hit -> xml.substring(hit.start(), hit.end()));
    }

    /// @param xml XML text
    /// @param tag Element name
    /// @return Unescaped, trimmed text of the first `<tag>...</tag>` element, if any
    public static Optional<String> text(String xml, String tag) {
        return elements(xml, tag)
            .findFirst()
            .map(String::trim)
            .map(Xml::unescape);
    }

    /// @param text Text to escape
    /// @return Text that is safe as XML element content
    public static String escape(String text) {
        return text.chars()
            .mapToObj(Xml::escape)
            .collect(Collectors.joining());
    }

    /// @param text Element content
    /// @return Content with predefined entities and character references resolved
    public static String unescape(String text) {
        return text.indexOf('&') < 0
            ? text
            : ENTITY.matcher(text).replaceAll(match ->
                Matcher.quoteReplacement(resolve(match.group(1))));
    }

    private Xml() {
    }

    private static final Pattern ENTITY = Pattern.compile("&(#x[0-9a-fA-F]+|#[0-9]+|amp|lt|gt|quot|apos);");

    private static Hit find(String xml, String open, String close, int from) {
        var openIndex = xml.indexOf(open, from);
        if (openIndex < 0) {
            return null;
        }
        var start = openIndex + open.length();
        var end = xml.indexOf(close, start);
        if (end < 0) {
            throw new IllegalStateException("No closing tag for " + open + " after index " + start);
        }
        return new Hit(start, end, end + close.length());
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    private static String escape(int c) {
        return switch (c) {
            case '&' -> "&amp;";
            case '<' -> "&lt;";
            case '>' -> "&gt;";
            case '"' -> "&quot;";
            case '\'' -> "&apos;";
            case '\r' -> "&#xD;";
            case '\n' -> "&#xA;";
            default -> String.valueOf((char) c);
        };
    }

    private static String resolve(String entity) {
        return switch (entity) {
            case "amp" -> "&";
            case "lt" -> "<";
            case "gt" -> ">";
            case "quot" -> "\"";
            case "apos" -> "'";
            default -> Character.toString(
                entity.startsWith("#x")
                    ? Integer.parseInt(entity.substring(2), 16)
                    : Integer.parseInt(entity.substring(1))
            );
        };
    }

    private record Hit(int start, int end, int next) {
    }
}
