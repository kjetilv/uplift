package com.github.kjetilv.uplift.json.gen;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.NullCallbacks;
import com.github.kjetilv.uplift.json.Token;
import com.github.kjetilv.uplift.json.TokenTrie;

import java.util.*;
import java.util.function.*;

public final class PresetCallbacks<B extends Supplier<T>, T extends Record> implements Callbacks {

    private final B builder;

    private final Callbacks parent;

    private final Consumer<T> onDone;

    private final TokenTrie tokenTrie;

    private final Map<Token.Field, BiConsumer<B, ? extends Number>> numbers;

    private final Map<Token.Field, BiConsumer<B, String>> strings;

    private final Map<Token.Field, BiConsumer<B, Boolean>> booleans;

    private final Map<Token.Field, BiFunction<Callbacks, B, Callbacks>> objects;

    private Token.Field currentField;

    public PresetCallbacks(
        B builder,
        Callbacks parent,
        Map<Token.Field, BiConsumer<B, ? extends Number>> numbers,
        Map<Token.Field, BiConsumer<B, String>> strings,
        Map<Token.Field, BiConsumer<B, Boolean>> booleans,
        Map<Token.Field, BiFunction<Callbacks, B, Callbacks>> objects,
        Consumer<T> onDone,
        TokenTrie tokenTrie
    ) {
        this.builder = Objects.requireNonNull(builder, "builder");
        this.parent = parent;
        this.numbers = Objects.requireNonNull(numbers, "numbers");
        this.strings = Objects.requireNonNull(strings, "strings");
        this.booleans = Objects.requireNonNull(booleans, "booleans");
        this.objects = Objects.requireNonNull(objects, "objects");
        this.onDone = Objects.requireNonNull(onDone, "onDone");
        this.tokenTrie = tokenTrie;
    }

    @Override
    public Callbacks objectStarted() {
        return currentField == null
            ? this
            : Optional.ofNullable(objects.get(currentField))
                .map(fun ->
                    fun.apply(this, builder))
                .orElseGet(() ->
                    new NullCallbacks(this));
    }

    @Override
    public Callbacks field(Token.Field token) {
        currentField = token;
        return this;
    }

    @Override
    public Callbacks objectEnded() {
        onDone.accept(builder.get());
        return parent == null ? this : parent;
    }

    @Override
    public Callbacks string(Token.String token) {
        if (currentField == null) {
            return fail();
        }
        BiConsumer<B, String> consumer = strings.get(currentField);
        if (consumer != null) {
            build(consumer, token.value());
        }
        return this;
    }

    @Override
    public Callbacks number(Token.Number number) {
        BiConsumer<B, Number> consumer = numberConsumer();
        if (consumer != null) {
            build(consumer, number.number());
        }
        return this;
    }

    @Override
    public Function<char[], Token.Field> tokenTrie() {
        return tokenTrie;
    }

    @Override
    public Callbacks bool(boolean bool) {
        BiConsumer<B, Boolean> consumer = booleans.get(currentField);
        if (consumer != null) {
            build(consumer, bool);
        }
        return this;
    }

    private <V, S extends V> void build(BiConsumer<B, V> consumer, S s) {
        try {
            consumer.accept(builder, s);
        } catch (Exception e) {
            throw new IllegalStateException(
                this + ": Failed to set " + s + (s == null ? "" : " of " + s.getClass()),
                e
            );
        }
    }

    @SuppressWarnings("unchecked")
    private <N extends Number> BiConsumer<B, N> numberConsumer() {
        return (BiConsumer<B, N>) numbers.get(currentField);
    }

    private <R> R fail() {
        throw new IllegalStateException("Unexpected object value for `" + currentField + '`');
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + builder + " -> " + parent + "]";
    }
}
