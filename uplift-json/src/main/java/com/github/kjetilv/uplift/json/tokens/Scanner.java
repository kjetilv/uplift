package com.github.kjetilv.uplift.json.tokens;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.kjetilv.uplift.json.tokens.TokenType.*;
import static java.lang.Character.isDigit;
import static java.lang.Character.isWhitespace;

public final class Scanner extends Spliterators.AbstractSpliterator<Token> {

    public static Stream<Token> tokens(String source) {
        return tokenStream(new CharSequenceSource(source));
    }

    public static Stream<Token> tokens(InputStream source) {
        return tokenStream(new BytesSource(source));
    }

    public static Stream<Token> tokens(Reader source) {
        return tokenStream(new CharsSource(source));
    }

    private final Source source;

    public Scanner(Source source) {
        super(Long.MAX_VALUE, IMMUTABLE | ORDERED);
        this.source = source;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Token> action) {
        if (source.done()) {
            return false;
        }
        try {
            Token token = scanToken();
            if (token != null) {
                action.accept(token);
            }
        } catch (Exception e) {
            return fail(this + " got unexpected error", e);
        } finally {
            source.reset();
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + source + "]";
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
            case ' ', '\r', '\t', '\n' -> {
                spoolWhitespace();
                yield null;
            }
            default ->
                fail("Unrecognized token");
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
                source.advance();
            }
            source.advance();
        }
        if (source.done()) {
            fail("Unterminated string");
        }
        source.advance();
        // Trim the surrounding quotes.
        String substring = source.lexeme(true);
        String literal = quoted
            ? ESCAPED_QUOTE.matcher(substring).replaceAll("\"")
            : substring;
        return token(STRING, literal, source.lexeme());
    }

    private Token number() {
        while (isDigit(source.peek())) {
            source.advance();
        }
        // Look for a fractional part.
        if (source.peek() == '.' && isDigit(source.peekNext())) {
            // Consume the "."
            do {
                source.advance();
            } while (isDigit(source.peek()));
        }
        return token(NUMBER, number(source.lexeme()), source.lexeme());
    }

    private void spoolWhitespace() {
        while (isWhitespace(source.peek())) {
            source.advance();
        }
    }

    private <T> T fail(String msg) {
        return fail(msg, null);
    }

    private <T> T fail(String msg, Throwable cause) {
        throw new ReadException(msg, source.lexeme(), source.line(), source.column() - 1, cause);
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
        return Long.parseLong(value);
    }

    public static Stream<Token> tokenStream(Source source) {
        return StreamSupport.stream(new Scanner(source), false);
    }
}
