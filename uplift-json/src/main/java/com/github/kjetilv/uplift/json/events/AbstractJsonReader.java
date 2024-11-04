package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.Source;
import com.github.kjetilv.uplift.json.tokens.Token;
import com.github.kjetilv.uplift.json.tokens.Tokens;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractJsonReader<S, T extends Record> implements JsonReader<S, T> {

    private final Function<Consumer<T>, Callbacks> callbacksInitializer;

    protected AbstractJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer) {
        this.callbacksInitializer = Objects.requireNonNull(callbacksInitializer, "callbacks");
    }

    @Override
    public final T read(S source) {
        AtomicReference<T> reference = new AtomicReference<>();
        read(source, reference::set);
        return reference.get();
    }

    @Override
    public final void read(S source, Consumer<T> setter) {
        Callbacks callbacks = this.callbacksInitializer.apply(setter);
        Tokens tokens = new Tokens(input(source));
        EventHandler handler = new ValueEventHandler(callbacks);
        while (true) {
            Token token = tokens.next();
            if (token == null) {
                return;
            }
            handler = handler.handle(token);
        }
    }

    protected abstract Source input(S source);
}
