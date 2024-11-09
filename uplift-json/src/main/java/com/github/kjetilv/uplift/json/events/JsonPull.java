package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.*;

import java.io.InputStream;
import java.io.Reader;

public final class JsonPull {

    public static Callbacks parse(Callbacks callbacks, String source) {
        return pull(new CharSequenceSource(source), callbacks);
    }

    @SuppressWarnings("unused")
    public static Callbacks parse(Callbacks callbacks, InputStream source) {
        return pull(new InputStreamSource(source), callbacks);
    }

    @SuppressWarnings("unused")
    public static Callbacks parse(Callbacks callbacks, Reader source) {
        return pull(new CharsSource(source), callbacks);
    }

    public static Callbacks parse(Source source, Callbacks callbacks) {
        return pull(source, callbacks);
    }

    private JsonPull() {
    }

    private static Callbacks pull(Source source, Callbacks callbacks) {
        Tokens tokens = new Tokens(source);
        JsonPullParser parser = new JsonPullParser(tokens);
        return parser.pull(callbacks);
    }
}
