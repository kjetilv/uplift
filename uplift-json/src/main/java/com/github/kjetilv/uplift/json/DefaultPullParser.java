package com.github.kjetilv.uplift.json;

import com.github.kjetilv.uplift.json.io.ReadException;

import static com.github.kjetilv.uplift.json.TokenType.*;

final class DefaultPullParser implements PullParser {

    @Override
    public Callbacks pull(Tokens tokens, Callbacks callbacks) {
        return processValue(tokens, tokens.next(), callbacks, 0);
    }

    /// Maximum nesting of objects and arrays. Deeper input is rejected, instead of exhausting the stack.
    static final int MAX_DEPTH = 1000;

    private Callbacks processValue(Tokens tokens, Token token, Callbacks callbacks, int depth) {
        return switch (token) {
            case Token.Str str -> callbacks.string(str);
            case Token.BeginObject _ -> processObject(tokens, callbacks, nested(tokens, depth));
            case Token.BeginArray _ -> processArray(tokens, callbacks, nested(tokens, depth));
            case Token.Null _ -> callbacks.nuul();
            case Token.True _ -> callbacks.bool(true);
            case Token.False _ -> callbacks.bool(false);
            case Token.Number number -> callbacks.number(number);
            default -> failParse(
                tokens,
                token,
                BEGIN_OBJECT,
                BEGIN_ARRAY,
                NULL,
                STRING,
                NUMBER,
                BOOL
            );
        };
    }

    private Callbacks processObject(Tokens tokens, Callbacks initial, int depth) {
        var callbacks = initial.objectStarted();
        var canonical = callbacks.tokenResolver().isPresent();
        var next = tokens.nextField(canonical);
        while (next != Token.END_OBJECT) {
            switch (next) {
                case Token.Field fieldToken -> callbacks = processField(tokens, fieldToken, callbacks, depth);
                case Token.SkipField _ -> skip(tokens);
                default -> failParse(tokens, next, STRING, TokenType.END_OBJECT);
            }
            next = commaOr(tokens, Token.END_OBJECT, true, canonical);
        }
        return callbacks.objectEnded();
    }

    private Callbacks processArray(Tokens tokens, Callbacks initial, int depth) {
        var callbacks = initial.arrayStarted();
        var next = tokens.next();
        while (next != Token.END_ARRAY) {
            callbacks = processValue(tokens, next, callbacks, depth);
            next = commaOr(tokens, Token.END_ARRAY, false, false);
        }
        return callbacks.arrayEnded();
    }

    private Callbacks processField(Tokens tokens, Token.Field field, Callbacks initial, int depth) {
        try {
            var callbacks = initial.field(field);
            tokens.skipNext(Token.COLON);
            var valueToken = tokens.next();
            return processValue(tokens, valueToken, callbacks, depth);
        } catch (ReadException e) {
            throw e.inField(field.value());
        } catch (FieldException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new FieldException(field, e);
        }
    }

    private void skip(Tokens tokens) {
        var token = tokens.skipNext(Token.COLON).next();
        if (token == Token.BEGIN_OBJECT) {
            skipStructure(tokens, Token.BEGIN_OBJECT, Token.END_OBJECT);
        } else if (token == Token.BEGIN_ARRAY) {
            skipStructure(tokens, Token.BEGIN_ARRAY, Token.END_ARRAY);
        } else if (!token.isValue()) {
            failParse(tokens, token, BEGIN_OBJECT, BEGIN_ARRAY, BOOL, NULL, NUMBER, STRING);
        }
    }

    private void skipStructure(Tokens tokens, Token opening, Token closing) {
        var levels = 0;
        do {
            var next = tokens.next();
            if (next == closing) {
                levels--;
            } else if (next == opening) {
                levels++;
            }
        } while (levels >= 0);
    }

    private Token commaOr(Tokens tokens, Token closing, boolean fieldName, boolean canonical) {
        var token = tokens.next(false, false);
        if (token == Token.COMMA) {
            var nextToken = tokens.next(fieldName, canonical);
            return nextToken == closing
                ? failParse(tokens, nextToken, TokenType.valueTokens())
                : nextToken;
        }
        return token == closing
            ? token
            : failParse(tokens, token, COMMA);
    }

    /// @return Depth of a new nested object or array
    /// @throws ReadException If the depth exceeds [#MAX_DEPTH]
    private static int nested(Tokens tokens, int depth) {
        if (depth >= MAX_DEPTH) {
            throw new ReadException("Nesting deeper than " + MAX_DEPTH, null, tokens.position());
        }
        return depth + 1;
    }

    private <T> T failParse(Tokens tokens, Token actual, TokenType... expected) {
        throw new ParseException(this, tokens.position(), actual, expected);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[]";
    }

    /// Failure in callbacks for a field. Invalid input is not wrapped, and this is wrapped only once.
    private static final class FieldException extends IllegalStateException {

        private FieldException(Token.Field field, RuntimeException cause) {
            super("Failed to set `" + field.value() + "`", cause);
        }
    }
}