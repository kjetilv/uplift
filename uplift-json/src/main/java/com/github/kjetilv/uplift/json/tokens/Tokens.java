package com.github.kjetilv.uplift.json.tokens;

import com.github.kjetilv.uplift.json.Source;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.kjetilv.uplift.json.tokens.Token.*;
import static com.github.kjetilv.uplift.json.tokens.TokenType.*;
import static java.lang.Character.isDigit;
import static java.lang.Character.isWhitespace;

public final class Tokens implements Supplier<Token>, SelfDescribing {

    private final Source source;

    public Tokens(Source source) {
        this.source = source;
    }

    public Stream<Token> stream() {
        return Stream.generate(this).takeWhile(Objects::nonNull);
    }

    public boolean done() {
        spool();
        return source.done();
    }

    @Override
    public Token get() {
        return next();
    }

    public Token next() {
        while (true) {
            if (source.done()) {
                return null;
            }
            try {
                Token token = scanToken();
                if (token == null) {
                    return null;
                }
                if (token == CANONICAL_WHITESPACE) {
                    continue;
                }
                return token;
            } finally {
                source.reset();
            }
        }
    }

    @Override
    public String bringIt() {
        return MessageFormat.format("`{0}` [{1}:{2}]", source.lexeme(), source.line(), source.column() - 1);
    }

    private Token scanToken() {
        return switch (source.chomp()) {
            case '{' -> BEGIN_OBJECT_TOKEN;
            case ':' -> COLON_TOKEN;
            case ',' -> COMMA_TOKEN;
            case '}' -> END_OBJECT_TOKEN;
            case '[' -> BEGIN_ARRAY_TOKEN;
            case ']' -> END_ARRAY_TOKEN;
            case 't' -> expectedTokenTail(_RUE, BOOL, true, CANONICAL_TRUE);
            case 'f' -> expectedTokenTail(_ALSE, BOOL, false, CANONICAL_FALSE);
            case 'n' -> expectedTokenTail(_ULL, NULL, null, CANONICAL_NULL);
            case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' -> number();
            case '"' -> string();
            case ' ', '\r', '\t', LN -> spool();
            default -> fail("Unrecognized character");
        };
    }

    private Token expectedTokenTail(
        char[] tail,
        TokenType type,
        Object literal,
        String canonical
    ) {
        for (char c : tail) {
            if (source.chomp() != c) {
                fail("Unknown identifier");
            }
        }
        return token(type, literal, canonical == null ? source.lexeme() : canonical);
    }

    private Token string() {
        boolean quoted = false;
        while (source.peek() != '"' && !source.done()) {
            char next = source.peek();
            if (next == LN) {
                fail("Line break in string: " + source.lexeme());
            }
            if (next == QUOTE) {
                quoted = true;
                source.chomp();
            }
            source.chomp();
        }
        if (source.done()) {
            fail("Unterminated string");
        }
        source.chomp();
        // Trim the surrounding quotes.
        String substring = source.quotedLexeme();
        String literal = quoted
            ? ESCAPED_QUOTE.matcher(substring).replaceAll("\"")
            : substring;
        return token(STRING, literal, source.lexeme());
    }

    private Token number() {
        while (isDigit(source.peek())) {
            source.chomp();
        }
        // Look for a fractional part.
        if (source.peek() == '.' && isDigit(source.peekNext())) {
            // Consume the "."
            do {
                source.chomp();
            } while (isDigit(source.peek()));
        }
        return token(NUMBER, number(source.lexeme()), source.lexeme());
    }

    private Token spool() {
        while (isWhitespace(source.peek())) {
            source.chomp();
        }
        return CANONICAL_WHITESPACE;
    }

    private Token token(TokenType type, Object literal, String lexeme) {
        int line = source.line();
        int col = source.column() - lexeme.length();
        return new Token(type, lexeme, literal, line, col);
    }

    private <T> T fail(String msg) {
        throw new ReadException(msg, bringIt());
    }

    static final String CANONICAL_TRUE = "true";

    static final String CANONICAL_FALSE = "false";

    private static final char[] _RUE = "rue".toCharArray();

    private static final char[] _ALSE = "alse".toCharArray();

    private static final char[] _ULL = "ull".toCharArray();

    private static final String CANONICAL_NULL = "null";

    private static final Pattern ESCAPED_QUOTE = Pattern.compile("\\\\\"");

    private static final String CANONICAL_LEFT_BRA = "[";

    private static final String CANONICAL_RIGHT_BRA = "]";

    private static final char QUOTE = '\\';

    private static final char LN = '\n';

    private static Number number(String value) {
        try {
            BigDecimal dec = new BigDecimal(value);
            return dec.scale() == 0 ? dec.longValue() : dec;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse: " + value, e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + CANONICAL_LEFT_BRA + bringIt() + CANONICAL_RIGHT_BRA;
    }
}
