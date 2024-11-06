package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.*;

import java.io.InputStream;
import java.io.Reader;

public final class Events {

    public static Callbacks parse(Callbacks callbacks, String source) {
        return callbacks(callbacks, new CharSequenceSource(source));
    }

    @SuppressWarnings("unused")
    public static Callbacks parse(Callbacks callbacks, InputStream source) {
        return callbacks(callbacks, new InputStreamSource(source));
    }

    @SuppressWarnings("unused")
    public static Callbacks parse(Callbacks callbacks, Reader source) {
        return callbacks(callbacks, new CharsSource(source));
    }

    public static Callbacks parse(Callbacks callbacks, Source source) {
        return callbacks(callbacks, source);
    }

    private static Callbacks callbacks(Callbacks callbacks, Source source) {
        Tokens tokens = new Tokens(source);
        EventHandler handler = new ValueEventHandler(callbacks);
        while (true) {
            Token token = tokens.next();
            if (token == null) {
                return handler.callbacks();
            }
            handler = handler.handle(token);
        }
    }

    private Events() {
    }
}
