package com.github.kjetilv.uplift.json.tokens;

final class Strings {

    static String unquote(String substring) {
        char[] charArray = substring.toCharArray();
        int quotedLength = charArray.length;
        int quotes = 0;
        for (int i = 0; i < quotedLength - quotes; i++) {
            if (charArray[i + quotes] == '\\' && charArray[i + quotes + 1] == '"') {
                quotes++;
            }
            charArray[i] = charArray[i + quotes];
        }
        return new String(charArray, 0, quotedLength - quotes);
    }

    private Strings() {
    }
}
