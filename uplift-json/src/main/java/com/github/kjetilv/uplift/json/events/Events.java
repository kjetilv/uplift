package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;

import java.io.InputStream;
import java.io.Reader;

final class Events {

    static Callbacks parse(Callbacks callbacks, String source) {
        return EventHandler.parse(callbacks, source);
    }

    @SuppressWarnings("unused")
    static Callbacks parse(Callbacks callbacks, InputStream source) {
        return EventHandler.parse(callbacks, source);
    }

    @SuppressWarnings("unused")
    static Callbacks parse(Callbacks callbacks, Reader source) {
        return EventHandler.parse(callbacks, source);
    }

    private Events() {
    }
}
