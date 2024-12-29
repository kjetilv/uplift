package com.github.kjetilv.uplift.json;

import static com.github.kjetilv.uplift.json.Token.COLON;
import static com.github.kjetilv.uplift.json.Token.END_OBJECT;
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
        while (true) {
            if (next instanceof Token.Field fieldToken) {
                Callbacks fieldCallbacks = objectCallbacks.field(fieldToken);
                tokens.skipNext(COLON);
                Token valueToken = tokens.next();
                objectCallbacks = processValue(valueToken, fieldCallbacks);
                next = commaOr(END_OBJECT, canonical);
            } else {
                return next == END_OBJECT
                    ? objectCallbacks.objectEnded()
                    : failParse(next, STRING, TokenType.END_OBJECT);
            }
        }
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
            Token nextToken = tokens.next(closing == END_OBJECT, canonical);
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