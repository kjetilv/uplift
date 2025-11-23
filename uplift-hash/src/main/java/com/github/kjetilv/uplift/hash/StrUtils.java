package com.github.kjetilv.uplift.hash;

final class StrUtils {

    static String normalize(String base64, int endIndex) {
        return base64.substring(0, endIndex)
            .replace(BAD_1, GOOD_1)
            .replace(BAD_2, GOOD_2);
    }

    static String denormalize(String raw) {
        return raw
            .replace(GOOD_1, BAD_1)
            .replace(GOOD_2, BAD_2);
    }

    static String par(Object object) {
        return LPAR + object + RPAR;
    }

    private StrUtils() {
    }

    private static final String LPAR = "⟨";

    private static final String RPAR = "⟩";

    private static final char GOOD_1 = '-';

    private static final char GOOD_2 = '_';

    private static final char BAD_2 = '+';

    private static final char BAD_1 = '/';
}
