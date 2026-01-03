package com.github.kjetilv.uplift.json.gen;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

final class FromMap implements Consumer<Object> {

    private final Callbacks callbacks;

    FromMap(Callbacks callbacks) {
        this.callbacks = Objects.requireNonNull(callbacks, "callbacks");
    }

    @Override
    public void accept(Object value) {
        switch (value) {
            case null -> callbacks.nuul();
            case String string -> callbacks.string(new Token.Str(string));
            case Number number -> callbacks.number(new Token.Number(number));
            case Boolean b -> callbacks.bool(b);
            case Map<?, ?> map -> doMap(map);
            case Iterable<?> list -> doArray(list);
            default -> throw new IllegalStateException(
                "Not a valid value: " + value + " of " + value.getClass()
            );
        }
    }

    private void doMap(Map<?, ?> map) {
        var objectCallbacks = callbacks.objectStarted();
        var objectFromMap = new FromMap(objectCallbacks);
        objectFromMap.acceptFields(map);
        objectCallbacks.objectEnded();
    }

    private void acceptFields(Map<?, ?> map) {
        map.forEach((key, value) -> {
            var field = new Token.Field(key.toString());
            callbacks.field(resolve(field));
            accept(value);
        });
    }

    private void doArray(Iterable<?> list) {
        try {
            callbacks.arrayStarted();
            for (Object item : list) {
                accept(item);
            }
        } finally {
            callbacks.arrayEnded();
        }
    }

    private Token.Field resolve(Token.Field field) {
        return callbacks.tokenResolver()
            .map(resolver ->
                resolver.get(field))
            .orElse(field);
    }
}
