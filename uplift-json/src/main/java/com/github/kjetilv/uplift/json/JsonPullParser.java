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
            case Token.BeginObject _ -> processObject(callbacks);
            case Token.BeginArray _ -> processArray(callbacks);
            case Token.Null _ -> callbacks.nuul();
            case Token.True _ -> callbacks.bool(true);
            case Token.False _ -> callbacks.bool(false);
            case Token.String string -> callbacks.string(string);
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
        Callbacks objectCallbacks = callbacks.objectStarted();
        boolean canonical = objectCallbacks.canonical();
        Token next = tokens.nextField(canonical);
        do {
            if (next == Token.END_OBJECT) {
                return objectCallbacks.objectEnded();
            }
            if (next == Token.SKIP_FIELD) {
                tokens.skipNext(Token.COLON);
                skipValue(tokens.next());
                next = commaOr(Token.END_OBJECT, canonical);
            } else if (next instanceof Token.Field fieldToken) {
                try {
                    Callbacks fieldCallbacks = objectCallbacks.field(fieldToken);
                    tokens.skipNext(Token.COLON);
                    Token valueToken = tokens.next();
                    objectCallbacks = processValue(valueToken, fieldCallbacks);
                    next = commaOr(Token.END_OBJECT, canonical);
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to set `" + fieldToken.value() + "`", e);
                }
            } else {
                failParse(next, STRING, TokenType.END_OBJECT);
            }
        } while (true);
    }

    private void skipValue(Token token) {
        if (token == Token.BEGIN_OBJECT) {
            skipStructure(Token.BEGIN_OBJECT, Token.END_OBJECT);
        } else if (token == Token.BEGIN_ARRAY) {
            skipStructure(Token.BEGIN_ARRAY, Token.END_ARRAY);
        } else if (!token.isValue()) {
            failParse(token, BEGIN_OBJECT, BEGIN_ARRAY, BOOL, NULL, NUMBER, STRING);
        }
    }

    private void skipStructure(Token openingToken, Token closingToken) {
        int toClose = 0;
        do {
            Token next = tokens.next();
            if (next == closingToken) {
                toClose--;
            } else if (next == openingToken) {
                toClose++;
            }
        } while (toClose >= 0);
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

    private Token commaOr(Token closing, boolean canonical) {
        Token token = tokens.next();
        if (token == Token.COMMA) {
            Token nextToken = tokens.next(closing == Token.END_OBJECT, canonical);
            return nextToken == closing
                ? failParse(nextToken, closing.tokenType())
                : nextToken;
        }
        return token == closing
            ? token
            : failParse(token, COMMA, closing.tokenType());
    }

    private <T> T failParse(Token actual, TokenType... expected) {
        throw new ParseException(this, actual, expected);
    }
}