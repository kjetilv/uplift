package com.github.kjetilv.uplift.json.bytes;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class Numbers {

    static Number parseNumber(byte[] bytes, int offset, int length) {
        long value = 0;
        int dotIndex = -1;
        boolean negate = bytes[offset] == '-';
        if (negate && length == 1) {
            throw new NumberFormatException("Not a number: " + new String(bytes, offset, length));
        }
        int digits = negate ? length - 1 : length;
        int signOffset = negate ? 1 : 0;
        for (int i = 0; i < digits; i++) {
            byte b = bytes[offset + signOffset + i];
            if (b == '.') {
                dotIndex = i;
            } else {
                int digit = b - '0';
                if (digit < 0 || digit > 9) {
                    throw new NumberFormatException("Invalid digit: " + digit);
                }
                value = value * 10 + digit;
            }
        }
        if (dotIndex == -1) {
            return negate ? -value : value;
        } else {
            if (length == 1) {
                throw new NumberFormatException("Not a number: " + new String(bytes, offset, length));
            }
            if (negate && length == 2) {
                throw new NumberFormatException("Not a number: " + new String(bytes, offset, length));
            }
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

    private Numbers() {
    }
}
