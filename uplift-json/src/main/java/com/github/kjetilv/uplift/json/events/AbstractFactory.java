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

    protected AbstractFactory(Function<Consumer<T>, C> newCallbacks) {
        this.newCallbacks = newCallbacks;
    }

    public T read(String string) {
        return extract(consumer -> read(string, consumer));
    }

    public T read(InputStream inputStream) {
        return extract(setter -> read(inputStream,setter));
    }

    public T read(Reader reader) {
        return extract(setter -> read(reader, setter));
    }

    public void read(String string, Consumer<T> set) {
        Events.parse(callbacks(set), string);
    }

    public void read(Reader reader, Consumer<T> set) {
        Events.parse(callbacks(set), reader);
    }

    public void read(InputStream string, Consumer<T> set) {
        Events.parse(callbacks(set), string);
    }

    private C callbacks(Consumer<T> set) {
        return newCallbacks.apply(set);
    }

    private static <T> T extract(Consumer<Consumer<T>> consumer) {
        AtomicReference<T> reference = new AtomicReference<>();
        Consumer<T> set = reference::set;
        consumer.accept(set);
        return reference.get();
    }
}
