package com.github.kjetilv.uplift.json.tokens;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class Numbers {

    private Numbers() {
    }

    static Number parseNumber(byte[] bytes, int offset, int length) {
        long value = 0;
        int dotIndex = -1;
        boolean negate = bytes[offset] == '-';
        int digits = negate ? length - 1 : length;
        int signOffset = negate ? 1 : 0;
        for (int i = 0; i < digits; i++) {
            byte b = bytes[offset + signOffset + i];
            if (b == '.') {
                dotIndex = i;
            } else {
                value = value * 10 + (b - '0');
            }
        }
        if (dotIndex == -1) {
            return negate ? -value : value;
        }
        int decimals = digits - dotIndex - 1;
        long dim = (long) Math.pow(10, decimals);
        BigDecimal decimal = new BigDecimal(value).divide(
            new BigDecimal(dim),
            decimals,
            RoundingMode.UNNECESSARY
        );
        return negate ? decimal.negate() : decimal;
    }
}
