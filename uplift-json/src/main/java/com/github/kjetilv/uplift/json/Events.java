package com.github.kjetilv.uplift.json;

import java.io.InputStream;
import java.io.Reader;

import com.github.kjetilv.uplift.json.events.EventHandler;

public final class Events {

    public static <C extends Callbacks<C>> C parse(C callbacks, String source) {
        return EventHandler.parse(callbacks, source);
    }

    public static <C extends Callbacks<C>> C parse(C callbacks, InputStream source) {
        return EventHandler.parse(callbacks, source);
    }

    public static <C extends Callbacks<C>> C parse(C callbacks, Reader source) {
        return EventHandler.parse(callbacks, source);
    }

    @SuppressWarnings("unchecked")
    public interface Callbacks<C extends Callbacks<C>> {

        default C objectStarted() {
            return (C) this;
        }

        default C field(String name) {
            return (C) this;
        }

        default C objectEnded() {
            return (C) this;
        }

        default C arrayStarted() {
            return (C) this;
        }

        default C string(String string) {
            return (C) this;
        }

        default <N extends Number> C number(N number) {
            return (C) this;
        }

        default C truth(boolean truth) {
            return (C) this;
        }

        default C nil() {
            return (C) this;
        }

        default C arrayEnded() {
            return (C) this;
        }
    }

    private Events() {
    }
}
