package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Events;

import java.io.InputStream;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AbstractFactory<B extends Supplier<T>, T, C extends AbstractCallbacks<B, T>> {

    private final Function<Consumer<T>, C> newCallbacks;

    protected AbstractFactory(
        Function<Consumer<T>, C> newCallbacks
    ) {
        this.newCallbacks = newCallbacks;
    }

    public T read(String string) {
        AtomicReference<T> reference = new AtomicReference<>();
        Consumer<T> set = reference::set;
        read(string, set);
        return reference.get();
    }

    public T read(InputStream inputStream) {
        AtomicReference<T> reference = new AtomicReference<>();
        Consumer<T> set = reference::set;
        read(inputStream, set);
        return reference.get();
    }

    public T read(Reader reader) {
        AtomicReference<T> reference = new AtomicReference<>();
        Consumer<T> set = reference::set;
        read(reader, set);
        return reference.get();
    }

    public void read(String string, Consumer<T> set) {
        Events.parse(newCallbacks.apply(set), string);
    }

    public void read(Reader reader, Consumer<T> set) {
        Events.parse(newCallbacks.apply(set), reader);
    }

    public void read(InputStream string, Consumer<T> set) {
        Events.parse(newCallbacks.apply(set), string);
    }
}
