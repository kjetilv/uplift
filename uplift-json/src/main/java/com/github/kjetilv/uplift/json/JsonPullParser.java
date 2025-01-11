package com.github.kjetilv.uplift.json;

import static com.github.kjetilv.uplift.json.TokenType.*;

public final class JsonPullParser {

    private final Tokens tokens;

    public JsonPullParser(Tokens tokens) {
        this.tokens = tokens;
    }

    public Callbacks pull(Callbacks callbacks) {
        return processValue(tokens.next(), callbacks);
    }

    public boolean done() {
        return this.tokens.done();
    }

    private Callbacks processValue(Token token, Callbacks callbacks) {
        return switch (token) {
            case Token.Str str -> callbacks.string(str);
            case Token.BeginObject _ -> processObject(callbacks);
            case Token.BeginArray _ -> processArray(callbacks);
            case Token.Null _ -> callbacks.nuul();
            case Token.True _ -> callbacks.bool(true);
            case Token.False _ -> callbacks.bool(false);
            case Token.Number number -> callbacks.number(number);
            default -> failParse(
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

    private Callbacks processObject(Callbacks callbacks) {
        Callbacks cb = callbacks.objectStarted();
        boolean canonical = cb.canonical();
        Token next = tokens.nextField(canonical);
        do {
            if (next instanceof Token.Field fieldToken) {
                cb = processField(fieldToken, cb);
            } else if (next == Token.SKIP_FIELD) {
                skip();
            } else {
                return next == Token.END_OBJECT
                    ? cb.objectEnded()
                    : failParse(next, STRING, TokenType.END_OBJECT);
            }
            next = commaOr(Token.END_OBJECT, canonical);
        } while (true);
    }

    private Callbacks processArray(Callbacks callbacks) {
        Callbacks c = callbacks.arrayStarted();
        Token next = tokens.next();
        while (next != Token.END_ARRAY) {
            c = processValue(next, c);
            next = commaOr(Token.END_ARRAY, false);
        }
        return c.arrayEnded();
    }

    private Callbacks processField(Token.Field field, Callbacks callbacks) {
        try {
            Callbacks fieldCallbacks = callbacks.field(field);
            tokens.skipNext(Token.COLON);
            Token valueToken = tokens.next();
            return processValue(valueToken, fieldCallbacks);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set `" + field.value() + "`", e);
        }
    }

    private void skip() {
        tokens.skipNext(Token.COLON);
        Token token = tokens.next();
        if (token == Token.BEGIN_OBJECT) {
            skipStructure(Token.BEGIN_OBJECT, Token.END_OBJECT);
        } else if (token == Token.BEGIN_ARRAY) {
            skipStructure(Token.BEGIN_ARRAY, Token.END_ARRAY);
        } else if (!token.isValue()) {
            failParse(token, BEGIN_OBJECT, BEGIN_ARRAY, BOOL, NULL, NUMBER, STRING);
        }
    }

    private void skipStructure(Token opening, Token closing) {
        int levels = 0;
        do {
            Token next = tokens.next();
            if (next == closing) {
                levels--;
            } else if (next == opening) {
                levels++;
            }
        } while (levels >= 0);
    }

    private Token commaOr(Token closing, boolean canonical) {
        Token token = tokens.next();
        if (token == Token.COMMA) {
            Token nextToken = tokens.next(closing == Token.END_OBJECT, canonical);
            return nextToken == closing
                ? failParse(nextToken, TokenType.valueTokens())
                : nextToken;
        }
        return token == closing
            ? token
            : failParse(token, COMMA);
    }

    private <T> T failParse(Token actual, TokenType... expected) {
        throw new ParseException(this, actual, expected);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + tokens + ']';
    }
}