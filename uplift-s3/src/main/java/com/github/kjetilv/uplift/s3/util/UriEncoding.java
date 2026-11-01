package com.github.kjetilv.uplift.s3.util;

import module java.base;

/// URI encoding as required by AWS Signature Version 4, see RFC 3986.
///
/// Unreserved characters (`A-Z`, `a-z`, `0-9`, `-`, `_`, `.`, `~`) are kept as they are.
/// All other bytes of the UTF-8 encoding are written as `%XX`, with upper case hex digits.
/// This differs from [URLEncoder], which writes space as `+` and does not encode `*`.
public final class UriEncoding {

    /// @param value     Value to encode
    /// @param keepSlash If true, `/` is not encoded. Use this for paths.
    /// @return Encoded value
    public static String encode(String value, boolean keepSlash) {
        var bytes = value.getBytes(StandardCharsets.UTF_8);
        var encoded = new StringBuilder(bytes.length * 3);
        for (var b : bytes) {
            if (unreserved(b) || keepSlash && b == '/') {
                encoded.append((char) b);
            } else {
                encoded.append('%').append(HEX.toHexDigits(b));
            }
        }
        return encoded.toString();
    }

    private UriEncoding() {
    }

    private static final HexFormat HEX = HexFormat.of().withUpperCase();

    private static boolean unreserved(byte b) {
        return 'A' <= b && b <= 'Z' ||
               'a' <= b && b <= 'z' ||
               '0' <= b && b <= '9' ||
               b == '-' ||
               b == '_' ||
               b == '.' ||
               b == '~';
    }
}
