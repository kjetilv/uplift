package com.github.kjetilv.uplift.json;

import com.github.kjetilv.uplift.json.io.ReadException;

public interface Tokens {

    default Token next() {
        return next(false, false);
    }

    default Token next(boolean fieldName) {
        return next(fieldName, false);
    }

    default Token nextField(boolean canonical) {
        return next(true, canonical);
    }

    default Tokens skipNext(Token expected) {
        var token = next();
        if (token == expected) {
            return this;
        }
        throw new ParseException(this, position(), token, expected.tokenType());
    }

    /// @return Approximate byte offset of the next unread byte, or -1 if not known
    default long position() {
        return -1;
    }

    Token next(boolean fieldName, boolean canonical);

    boolean done();
}
