package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.TokensSpliterator;
import com.github.kjetilv.uplift.json.tokens.Source;
import com.github.kjetilv.uplift.json.tokens.Tokens;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.StreamSupport;

public abstract class AbstractJsonReader<S, T extends Record> implements JsonReader<S, T> {

    private final Function<Consumer<T>, Callbacks> callbacks;

    protected AbstractJsonReader(Function<Consumer<T>, Callbacks> callbacks) {
        this.callbacks = Objects.requireNonNull(callbacks, "callbacks");
    }

    @Override
    public final T read(S source) {
        AtomicReference<T> reference = new AtomicReference<>();
        read(source, reference::set);
        return reference.get();
    }

    @Override
    public final void read(S source, Consumer<T> setter) {
        reduce(setter, input(source));
    }

    protected abstract Source input(S source);

    @SuppressWarnings("UnusedReturnValue")
    private Callbacks reduce(Consumer<T> setter, Source source) {
        TokensSpliterator tokensSpliterator = new TokensSpliterator(new Tokens(source));
        EventHandler identity = new ValueEventHandler(callbacks.apply(setter));
        EventHandler handler = StreamSupport.stream(tokensSpliterator, false)
            .reduce(identity, EventHandler::apply, NO_COMBINE);
        return handler.callbacks();
    }

    public static final BinaryOperator<EventHandler> NO_COMBINE = (t1, t2) -> {
        throw new IllegalStateException(t1 + " / " + t2 + " do not combine");
    };
}
