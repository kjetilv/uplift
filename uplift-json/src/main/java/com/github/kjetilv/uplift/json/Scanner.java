package com.github.kjetilv.uplift.json;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.kjetilv.uplift.json.TokenType.*;

final class Scanner extends Spliterators.AbstractSpliterator<Token> {

    static Stream<Token> tokens(String source) {
        return tokenStream(new StringSource(source));
    }

    static Stream<Token> tokens(InputStream source) {
        return tokenStream(new BytesSource(source));
    }

    private final Source source;

    private Scanner(Source source) {
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

    private Token scanToken() {
        return switch (source.chomp()) {
            case '{' -> token(BEGIN_OBJECT);
            case ':' -> token(COLON);
            case ',' -> token(COMMA);
            case '}' -> token(END_OBJECT);
            case '[' -> token(BEGIN_ARRAY);
            case ']' -> token(END_ARRAY);
            case 't' -> expectedTokenTail(TRUE_TAIL, BOOL, CANONICAL_TRUE);
            case 'f' -> expectedTokenTail(FALSE_TAIL, BOOL, CANONICAL_FALSE);
            case 'n' -> expectedTokenTail(NULL_TAIL, NIL, CANONICAL_NULL);
            case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' -> number();
            case '"' -> string();
            case ' ', '\r', '\t' -> spaces(false);
            case '\n' -> spaces(true);
            default -> fail("Unrecognized token");
        };
    }

    private Token expectedTokenTail(char[] tail, TokenType type, String canonical) {
        for (char c: tail) {
            if (source.chomp() != c) {
                fail("Unknown identifier");
            }
        }
        return token(type, null, canonical);
    }

    @SuppressWarnings("QuestionableName")
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
        return token(STRING, quoted
            ? ESCAPED_QUOTE.matcher(substring).replaceAll("\"")
            : substring, null);
    }

    private Token number() {
        while (isDigit(source.peek())) {
            source.advance();
        }
        // Look for a fractional part.
        if (source.peek() == '.' && isDigit(source.peekNext())) {
            // Consume the "."
            source.advance();
            while (isDigit(source.peek())) {
                source.advance();
            }
        }
        return token(NUMBER, number(source.lexeme()), null);
    }

    private Token spaces(boolean newLine) {
        if (newLine) {
            newLine();
        }
        char peek;
        while (isWS(peek = source.peek())) {
            if (peek == '\n') {
                newLine();
            }
            source.advance();
        }
        return null;
    }

    private void newLine() {
    }

    private <T> T fail(String msg) {
        return fail(msg, null);
    }

    private <T> T fail(String msg, Throwable cause) {
        throw new ReadException(msg, source.lexeme(), source.line(), source.column() - 1, cause);
    }

    private Token token(TokenType type) {
        return token(type, null, null);
    }

    private Token token(TokenType type, Object literal, String canonical) {
        String lexeme = canonical == null ? source.lexeme() : canonical;
        return new Token(type, lexeme, literal, source.line(), source.column() - lexeme.length());
    }

    private static final char[] TRUE_TAIL = "rue".toCharArray();

    private static final char[] FALSE_TAIL = "alse".toCharArray();

    private static final char[] NULL_TAIL = "ull".toCharArray();

    private static final String CANONICAL_TRUE = "true";

    private static final String CANONICAL_FALSE = "false";

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

    private static Stream<Token> tokenStream(Source source) {
        return StreamSupport.stream(new Scanner(source), false);
    }

    private static boolean isDigit(char c) {
        return Character.isDigit(c) || c == '.';
    }

    private static boolean isWS(char c) {
        return Character.isWhitespace(c);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + source + "]";
    }
}
