package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.*;

import java.io.InputStream;
import java.io.Reader;

@FunctionalInterface
public interface EventHandler {

    static Callbacks parse(Callbacks callbacks, InputStream source) {
        return callbacks(callbacks, new InputStreamSource(source));
    }

    static Callbacks parse(Callbacks callbacks, Reader source) {
        return callbacks(callbacks, new CharsSource(source));
    }

    static Callbacks parse(Callbacks callbacks, String source) {
        return callbacks(callbacks, new CharSequenceSource(source));
    }

    default Callbacks callbacks() {
        throw new UnsupportedOperationException(this + " has no callbacks");
    }

    EventHandler handle(Token token);

    private static Callbacks callbacks(Callbacks callbacks, Source source1) {
        Tokens tokens = new Tokens(source1);
        EventHandler handler = init(callbacks);
        while (true) {
            Token token = tokens.next();
            if (token == null) {
                return handler.callbacks();
            }
            handler = handler.handle(token);
        }
    }

    private static EventHandler init(Callbacks callbacks) {
        return new ValueEventHandler(callbacks);
    }

}
