package com.github.kjetilv.uplift.json.tokens;

import com.github.kjetilv.uplift.json.Source;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
                if (token.is(WHITESPACE)) {
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
            case ' ', '\r', '\t', '\n' -> spool();
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
            if (next == '\n') {
                fail("Line break in string: " + source.lexeme());
            }
            if (next == '\\') {
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
        String substring = source.lexeme(true);
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
        Token ws = new Token(WHITESPACE, null, null, source.line(), source.column());
        while (isWhitespace(source.peek())) {
            source.chomp();
        }
        return ws;
    }

    private Token token(TokenType type, Object literal, String lexeme) {
        return token(type, literal, lexeme, null);
    }

    private Token token(TokenType type, Object literal, String lexeme, String flyweightLexeme) {
        int line = source.line();
        String value = flyweightLexeme == null ? lexeme : flyweightLexeme;
        int col = source.column() - (flyweightLexeme == null ? value.length() : 1);
        return new Token(
            type,
            value,
            literal,
            line,
            col
        );
    }

    private <T> T fail(String msg) {
        throw new ReadException(msg, bringIt());
    }

    public static final Token BEGIN_OBJECT_TOKEN = token(BEGIN_OBJECT);

    public static final Token COLON_TOKEN = token(COLON);

    public static final Token COMMA_TOKEN = token(COMMA);

    public static final Token END_OBJECT_TOKEN = token(END_OBJECT);

    public static final Token BEGIN_ARRAY_TOKEN = token(BEGIN_ARRAY);

    public static final Token END_ARRAY_TOKEN = token(END_ARRAY);

    static final String CANONICAL_TRUE = "true";

    static final String CANONICAL_FALSE = "false";

    private static final char[] _RUE = "rue".toCharArray();

    private static final char[] _ALSE = "alse".toCharArray();

    private static final char[] _ULL = "ull".toCharArray();

    private static final String CANONICAL_NULL = "null";

    private static final Pattern ESCAPED_QUOTE = Pattern.compile("\\\\\"");

    private static final String CANONICAL_LEFT_BRA = "[";

    private static final String CANONICAL_RIGHT_BRA = "]";

    private static Token token(TokenType type) {
        return new Token(type, null, null, -1, -1);
    }

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
