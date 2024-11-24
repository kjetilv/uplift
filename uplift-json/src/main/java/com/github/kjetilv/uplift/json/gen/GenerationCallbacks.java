package com.github.kjetilv.uplift.json.gen;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.NullCallbacks;
import com.github.kjetilv.uplift.json.Token;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings({"SameParameterValue", "unused"})
public class GenerationCallbacks<B extends Supplier<T>, T extends Record> implements Callbacks {

    private final Consumer<T> onDone;

    private final Callbacks parent;

    private final Map<Token.Field, BiConsumer<B, ? extends Number>> numbers;

    private final Map<Token.Field, BiConsumer<B, String>> strings;

    private final Map<Token.Field, BiConsumer<B, Boolean>> booleans;

    private final Map<Token.Field, BiFunction<Callbacks, B, Callbacks>> objects;

    private final B builder;

    private Token.Field currentField;

    public GenerationCallbacks(
        B builder,
        Callbacks parent,
        Map<Token.Field, BiConsumer<B, ? extends Number>> numbers,
        Map<Token.Field, BiConsumer<B, String>> strings,
        Map<Token.Field, BiConsumer<B, Boolean>> booleans,
        Map<Token.Field, BiFunction<Callbacks, B, Callbacks>> objectFields,
        Consumer<T> onDone
    ) {
        this.builder = builder;
        this.parent = parent;
        this.numbers = numbers;
        this.strings = strings;
        this.booleans = booleans;
        this.objects = objectFields;
        this.onDone = onDone;
    }

    @Override
    public final Callbacks objectStarted() {
        return currentField == null
            ? this
            : Optional.ofNullable(objects.get(currentField))
                .map(fun ->
                    fun.apply(this, builder))
                .orElseGet(() ->
                    new NullCallbacks(this));
    }

    @Override
    public final Callbacks field(Token.Field token) {
        currentField = token;
        return this;
    }

    @Override
    public final Callbacks objectEnded() {
        onDone.accept(builder.get());
        return parent == null ? this : parent;
    }

    @Override
    public final Callbacks string(Token.String token) {
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
    public final Callbacks number(Token.Number number) {
        BiConsumer<B, Number> consumer = numberConsumer();
        if (consumer != null) {
            build(consumer, number.number());
        }
        return this;
    }

    @Override
    public final Callbacks bool(boolean bool) {
        BiConsumer<B, Boolean> consumer = booleans.get(currentField);
        if (consumer != null) {
            build(consumer, bool);
        }
        return this;
    }

    @Override
    public Collection<Token.Field> canonicalTokens() {
        return Stream.of(numbers, strings, booleans, objects)
            .map(Map::keySet).flatMap(Set::stream).distinct()
            .toList();
    }

    protected B builder() {
        return builder;
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

    private <R> R fail(Object value) {
        throw new IllegalStateException("Unexpected value for `" + currentField + "`: " + value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + builder + " -> " + parent + "]";
    }
}
