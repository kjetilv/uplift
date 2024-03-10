package com.github.kjetilv.uplift.json;

import java.io.InputStream;
import java.io.Reader;

import com.github.kjetilv.uplift.json.events.EventHandler;

public final class Events {

    public static Callbacks parse(Callbacks callbacks, String source) {
        return EventHandler.parse(callbacks, source);
    }

    public static Callbacks parse(Callbacks callbacks, InputStream source) {
        return EventHandler.parse(callbacks, source);
    }

    public static Callbacks parse(Callbacks callbacks, Reader source) {
        return EventHandler.parse(callbacks, source);
    }

    private Events() {
    }
}
