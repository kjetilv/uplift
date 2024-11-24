package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.ParseException;
import com.github.kjetilv.uplift.json.Token;
import com.github.kjetilv.uplift.json.TokenType;
import com.github.kjetilv.uplift.json.tokens.Tokens;

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
            case Token.BeginObject() -> processObject(callbacks);
            case Token.BeginArray() -> processArray(callbacks);
            case Token.Null() -> callbacks.nuul();
            case Token.True() -> callbacks.bool(true);
            case Token.False() -> callbacks.bool(false);
            case Token.String string -> callbacks.string(string);
            case Token.Number number -> callbacks.number(number);
            default -> fail(
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

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private Callbacks processObject(Callbacks callbacks) {
        Callbacks c = callbacks.objectStarted();
        Token next = tokens.nextName();
        while (true) {
            switch (next) {
                case Token.Field fieldToken -> {
                    Callbacks field = c.field(fieldToken);
                    tokens.skipNext(COLON);
                    Token valueToken = tokens.next();
                    c = processValue(valueToken, field);
                    next = commaOr(END_OBJECT);
                }
                default -> {
                    return next == END_OBJECT
                        ? c.objectEnded()
                        : fail(next, STRING, TokenType.END_OBJECT);
                }
            }
        }
    }

    private Callbacks processArray(Callbacks callbacks) {
        Callbacks c = callbacks.arrayStarted();
        Token next = tokens.next();
        while (next != Token.END_ARRAY) {
            c = processValue(next, c);
            next = commaOr(Token.END_ARRAY);
        }
        return c.arrayEnded();
    }

    private Token commaOr(Token closing) {
        Token token = tokens.next();
        if (token == Token.COMMA) {
            Token nextToken = tokens.next(closing == END_OBJECT);
            return nextToken == closing
                ? fail(nextToken, closing.tokenType())
                : nextToken;
        }
        return token == closing
            ? token
            : fail(token, COMMA, closing.tokenType());
    }

    private <T> T fail(Token actual, TokenType... expected) {
        throw new ParseException(this, actual, expected);
    }
}