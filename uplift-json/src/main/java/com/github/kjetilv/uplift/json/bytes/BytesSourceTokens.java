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

    private final char[] buffer = new char[1024];

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
            case 'f' -> _alse();
            case 't' -> _rue();
            case 'n' -> _ull();
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
        return new Token.Field(lexeme.copyBytes());
    }

    private Token.Str stringToken() {
        return new Token.Str(bytesSource.spoolString().copyBytes());
    }

    private Token numberToken() {
        var lexeme = bytesSource.spoolNumber();
        var len = lexeme.length();
        var bytes = lexeme.bytes();
        if (!isValidNumber(bytes, len)) {
            throw new ReadException("Invalid number", "<" + lexeme.string() + ">", bytesSource.position());
        }
        var chars = len <= buffer.length ? buffer : new char[len];
        for (var i = 0; i < len; i++) {
            chars[i] = (char) bytes[i];
        }
        var number = new BigDecimal(chars, 0, len);
        return new Token.Number(number.scale() == 0 ? integer(number) : number);
    }

    /// @param number Number with scale 0
    /// @return [Long] if in range, otherwise [BigInteger]
    private static java.lang.Number integer(BigDecimal number) {
        return number.precision() <= MAX_SAFE_LONG_DIGITS ||
               number.compareTo(LONG_MIN) >= 0 && number.compareTo(LONG_MAX) <= 0
            ? number.longValue()
            : number.toBigInteger();
    }

    /// Any integer with this many digits or fewer fits in a long
    private static final int MAX_SAFE_LONG_DIGITS = 18;

    private static final BigDecimal LONG_MIN = BigDecimal.valueOf(Long.MIN_VALUE);

    private static final BigDecimal LONG_MAX = BigDecimal.valueOf(Long.MAX_VALUE);

    /// Checks the JSON number grammar: `-? (0 | [1-9][0-9]*) (.[0-9]+)? ([eE][+-]?[0-9]+)?`
    ///
    /// @param bytes Number characters, from index 0
    /// @param len   Number of characters
    /// @return true iff valid
    private static boolean isValidNumber(byte[] bytes, int len) {
        var i = 0;
        if (i < len && bytes[i] == '-') {
            i++;
        }
        if (i < len && bytes[i] == '0') {
            i++;
        } else if (i < len && bytes[i] >= '1' && bytes[i] <= '9') {
            i = digits(bytes, i, len);
        } else {
            return false;
        }
        if (i < len && bytes[i] == '.') {
            var start = i + 1;
            i = digits(bytes, start, len);
            if (i == start) {
                return false;
            }
        }
        if (i < len && (bytes[i] == 'e' || bytes[i] == 'E')) {
            i++;
            if (i < len && (bytes[i] == '+' || bytes[i] == '-')) {
                i++;
            }
            var start = i;
            i = digits(bytes, start, len);
            if (i == start) {
                return false;
            }
        }
        return i == len;
    }

    /// @return Index of the first non-digit at or after `from`
    private static int digits(byte[] bytes, int from, int len) {
        var i = from;
        while (i < len && bytes[i] >= '0' && bytes[i] <= '9') {
            i++;
        }
        return i;
    }

    private Token.False _alse() {
        bytesSource.skip('a', 'l', 's', 'e');
        return Token.FALSE;
    }

    private Token.True _rue() {
        bytesSource.skip('r', 'u', 'e');
        return Token.TRUE;
    }

    private Token.Null _ull() {
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
