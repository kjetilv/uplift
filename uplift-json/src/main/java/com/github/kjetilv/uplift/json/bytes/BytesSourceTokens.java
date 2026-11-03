package com.github.kjetilv.uplift.json.bytes;

import module java.base;
import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.Token;
import com.github.kjetilv.uplift.json.TokenResolver;
import com.github.kjetilv.uplift.json.Tokens;
import com.github.kjetilv.uplift.json.io.ReadException;

public final class BytesSourceTokens implements Tokens {

    private final BytesSource bytesSource;

    private final TokenResolver knownTokens;

    private char[] buffer = new char[0];

    public BytesSourceTokens(BytesSource bytesSource, TokenResolver knownTokens) {
        this.bytesSource = bytesSource;
        this.knownTokens = knownTokens;
    }

    @Override
    public boolean done() {
        return bytesSource.done();
    }

    @Override
    public long position() {
        return bytesSource.position();
    }

    @Override
    public Token next(boolean fieldName, boolean canonical) {
        return scanToken(fieldName, canonical);
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    private Token scanToken(boolean fieldName, boolean canonical) {
        var c = bytesSource.chomp();
        return switch (c) {
            case ':' -> Token.COLON;
            case ',' -> Token.COMMA;
            case '{' -> Token.BEGIN_OBJECT;
            case '}' -> Token.END_OBJECT;
            case '[' -> Token.BEGIN_ARRAY;
            case ']' -> Token.END_ARRAY;
            case '"' -> fieldName ? fieldToken(canonical) : stringToken();
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-' -> numberToken();
            case 'f' -> alse();
            case 't' -> rue();
            case 'n' -> ull();
            case 0 -> fail("Unexpected end of stream", bytesSource.lexeme().string());
            default -> fail("Unrecognized character", Character.toString((char) c));
        };
    }

    private Token fieldToken(boolean canonical) {
        var lexeme = bytesSource.spoolField();
        if (canonical) {
            var resolved = knownTokens.get(lexeme.bytes(), lexeme.offset(), lexeme.length());
            return resolved == null ? Token.SKIP_FIELD : resolved;
        }
        return TokenResolver.DEFAULT.get(lexeme.bytes(), lexeme.offset(), lexeme.length());
    }

    private Token.Str stringToken() {
        return new Token.Str(bytesSource.spoolString().string());
    }

    private Token numberToken() {
        var lexeme = bytesSource.spoolNumber();
        var number = parseNumber(lexeme.bytes(), lexeme.offset(), lexeme.length());
        if (number == null) {
            throw new ReadException("Invalid number", "<" + lexeme.string() + ">", bytesSource.position());
        }
        return new Token.Number(number);
    }

    /// Checks the JSON number grammar, `-? (0 | [1-9][0-9]*) (.[0-9]+)? ([eE][+-]?[0-9]+)?`, and parses.
    /// Numbers without exponent and with at most 18 digits are parsed directly; others go via [BigDecimal].
    ///
    /// @return [Long] for integers in range, [BigInteger] for larger integers, [BigDecimal] otherwise, or
    ///     null if the grammar is not followed
    private java.lang.Number parseNumber(byte[] bytes, int offset, int length) {
        var end = offset + length;
        var i = offset;
        var negative = i < end && bytes[i] == '-';
        if (negative) {
            i++;
        }
        var intStart = i;
        if (i < end && bytes[i] == '0') {
            i++;
        } else if (i < end && bytes[i] >= '1' && bytes[i] <= '9') {
            i = digits(bytes, i, end);
        } else {
            return null;
        }
        var intEnd = i;
        var fracStart = i;
        var fracEnd = i;
        if (i < end && bytes[i] == '.') {
            fracStart = i + 1;
            i = fracEnd = digits(bytes, fracStart, end);
            if (fracEnd == fracStart) {
                return null;
            }
        }
        var exponent = i < end && (bytes[i] == 'e' || bytes[i] == 'E');
        if (exponent) {
            i++;
            if (i < end && (bytes[i] == '+' || bytes[i] == '-')) {
                i++;
            }
            var expStart = i;
            i = digits(bytes, expStart, end);
            if (i == expStart) {
                return null;
            }
        }
        if (i != end) {
            return null;
        }
        var scale = fracEnd - fracStart;
        if (!exponent && intEnd - intStart + scale <= MAX_SAFE_LONG_DIGITS) {
            var unscaled = accumulate(bytes, fracStart, fracEnd, accumulate(bytes, intStart, intEnd, 0L));
            var signed = negative ? -unscaled : unscaled;
            return scale == 0 ? (java.lang.Number) signed : BigDecimal.valueOf(signed, scale);
        }
        if (buffer.length < length) {
            buffer = new char[Math.max(length, 64)];
        }
        var chars = buffer;
        for (var j = 0; j < length; j++) {
            chars[j] = (char) bytes[offset + j];
        }
        var number = new BigDecimal(chars, 0, length);
        return number.scale() == 0 ? integer(number) : number;
    }

    /// @param number Number with scale 0
    /// @return [Long] if in range, otherwise [BigInteger]
    private static java.lang.Number integer(BigDecimal number) {
        return number.precision() <= MAX_SAFE_LONG_DIGITS ||
               number.compareTo(LONG_MIN) >= 0 && number.compareTo(LONG_MAX) <= 0
            ? number.longValue()
            : number.toBigInteger();
    }

    /// @return Index of the first non-digit at or after `from`
    private static int digits(byte[] bytes, int from, int end) {
        var i = from;
        while (i < end && bytes[i] >= '0' && bytes[i] <= '9') {
            i++;
        }
        return i;
    }

    private static long accumulate(byte[] bytes, int from, int to, long initial) {
        var value = initial;
        for (var i = from; i < to; i++) {
            value = value * 10 + bytes[i] - '0';
        }
        return value;
    }

    /// Any integer with this many digits or fewer fits in a long
    private static final int MAX_SAFE_LONG_DIGITS = 18;

    private static final BigDecimal LONG_MIN = BigDecimal.valueOf(Long.MIN_VALUE);

    private static final BigDecimal LONG_MAX = BigDecimal.valueOf(Long.MAX_VALUE);

    private Token.False alse() {
        bytesSource.skip('a', 'l', 's', 'e');
        return Token.FALSE;
    }

    private Token.True rue() {
        bytesSource.skip('r', 'u', 'e');
        return Token.TRUE;
    }

    private Token.Null ull() {
        bytesSource.skip('u', 'l', 'l');
        return Token.NULL;
    }

    private Token fail(String msg, String details) {
        var position = bytesSource.position();
        var tail = Stream.generate(bytesSource::chomp)
            .takeWhile(i -> i > 0)
            .limit(100)
            .map(Character::toString)
            .collect(Collectors.joining());
        throw new ReadException(msg, "<" + details + "><" + tail + ">", position);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + bytesSource.lexeme().string() + "]";
    }
}
