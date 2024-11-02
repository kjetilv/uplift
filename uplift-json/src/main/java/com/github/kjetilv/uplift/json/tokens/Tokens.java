package com.github.kjetilv.uplift.json.tokens;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.kjetilv.uplift.json.tokens.TokenType.*;
import static java.lang.Character.isDigit;
import static java.lang.Character.isWhitespace;

public final class Tokens implements Supplier<Token>, SelfDescribing {

    public static Stream<Token> tokens(String source) {
        return tokenStream(new CharSequenceSource(source));
    }

    public static Stream<Token> tokens(InputStream source) {
        return tokenStream(new InputStreamSource(source));
    }

    public static Stream<Token> tokens(Reader source) {
        return tokenStream(new CharsSource(source));
    }

    public static Stream<Token> tokenStream(Source source) {
        return Stream.generate(new Tokens(source));
    }

    private final Source source;

    public Tokens(Source source) {
        this.source = source;
    }

    @Override
    public Token get() {
        while (!source.done()) {
            try {
                Token token = scanToken();
                if (token == null) {
                    return null;
                }
                if (token.is(WHITESPACE)){
                    continue;
                }
                return token;
            } finally {
                source.resetLexeme();
            }
        }
        return null;
    }

    @Override
    public String bringIt() {
        return "`" + source.lexeme() + "` [" + source.line() + ":" + (source.column() - 1) + "]";
    }

    private Token scanToken() {
        return switch (source.chomp()) {
            case '{' -> token(BEGIN_OBJECT, null, source.lexeme());
            case ':' -> token(COLON, null, source.lexeme());
            case ',' -> token(COMMA, null, source.lexeme());
            case '}' -> token(END_OBJECT, null, source.lexeme());
            case '[' -> token(BEGIN_ARRAY, null, source.lexeme());
            case ']' -> token(END_ARRAY, null, source.lexeme());
            case 't' -> expectedTokenTail(RUE, BOOL, true, CANONICAL_TRUE);
            case 'f' -> expectedTokenTail(ALSE, BOOL, false, CANONICAL_FALSE);
            case 'n' -> expectedTokenTail(ULL, NIL, null, CANONICAL_NULL);
            case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' -> number();
            case '"' -> string();
            case ' ', '\r', '\t', '\n' -> spoolWs();
            default -> fail("Unrecognized token");
        };
    }

    private Token ws() {
        return new Token(WHITESPACE, null, null, source.line(), source.column());
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

    private Token spoolWs() {
        while (isWhitespace(source.peek())) {
            source.chomp();
        }
        return ws();
    }

    private <T> T fail(String msg) {
        return fail(msg, null);
    }

    private <T> T fail(String msg, Throwable cause) {
        throw new ReadException(msg, bringIt(), cause);
    }

    private Token token(TokenType type, Object literal, String lexeme) {
        return new Token(type, lexeme, literal, source.line(), source.column() - lexeme.length());
    }

    static final String CANONICAL_TRUE = "true";

    static final String CANONICAL_FALSE = "false";

    private static final char[] RUE = "rue".toCharArray();

    private static final char[] ALSE = "alse".toCharArray();

    private static final char[] ULL = "ull".toCharArray();

    private static final String CANONICAL_NULL = "null";

    private static final Pattern ESCAPED_QUOTE = Pattern.compile("\\\\\"");

    private static Object number(String value) {
        if (value.contains(".")) {
            try {
                return new BigDecimal(value);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to parse: " + value, e);
            }
        }
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            throw new IllegalStateException("Not a number: " + value, e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + bringIt() + "]";
    }
}
