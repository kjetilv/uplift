package uplift.json;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static uplift.json.TokenType.*;

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
            case '{' -> token(BEGIN_OBJECT, null, null);
            case ':' -> token(COLON, null, null);
            case ',' -> token(COMMA, null, null);
            case '}' -> token(END_OBJECT, null, null);
            case '[' -> token(BEGIN_ARRAY, null, null);
            case ']' -> token(END_ARRAY, null, null);
            case 't' -> expectedToken("rue", BOOL, "true");
            case 'f' -> expectedToken("alse", BOOL, "false");
            case 'n' -> expectedToken("ull", NIL, "null");
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' -> number();
            case '"' -> string();
            case ' ', '\r', '\t' -> spaces(false);
            case '\n' -> spaces(true);
            default -> fail("Unrecognized token");
        };
    }

    private Token expectedToken(CharSequence tail, TokenType type, String canonical) {
        return tail.chars().allMatch(c -> source.chomp() == c)
            ? token(type, null, canonical)
            : fail("Unknown identifier");
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

    private Token token(TokenType type, Object literal, String canonical) {
        String lexeme = canonical == null ? source.lexeme() : canonical;
        return new Token(type, lexeme, literal, source.line(), source.column() - lexeme.length());
    }

    private static final Pattern ESCAPED_QUOTE = Pattern.compile("\\\\\"");

    private static Stream<Token> tokenStream(Source source) {
        return StreamSupport.stream(new Scanner(source), false);
    }

    private static Object number(String value) {
        if (value.contains(".")) {
            return value.endsWith(".")
                ? Long.parseLong(value.substring(0, value.length() - 1))
                : new BigDecimal(value);
        }
        return Long.parseLong(value);
    }

    private static boolean isDigit(char c) {
        return Character.isDigit(c) || c == '.';
    }

    private static boolean isWS(char c) {
        return Character.isWhitespace(c);
    }
}
