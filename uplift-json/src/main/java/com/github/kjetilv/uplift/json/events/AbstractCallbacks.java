package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Events;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings({"SameParameterValue", "unused"})
public abstract class AbstractCallbacks<B extends Supplier<T>, T> implements Events.Callbacks<AbstractCallbacks<?, ?>> {

    private final B builder;

    private final AbstractCallbacks<?, ?> parent;

    private final Consumer<T> onDone;

    private final Map<String, BiConsumer<B, ? extends Number>> numbers = new LinkedHashMap<>();

    private final Map<String, BiConsumer<B, String>> strings = new LinkedHashMap<>();

    private final Map<String, BiConsumer<B, Boolean>> truths = new LinkedHashMap<>();

    private final Map<String, Supplier<AbstractCallbacks<?, ?>>> objectFields = new LinkedHashMap<>();

    private String currentField;

    protected AbstractCallbacks(
        B builder,
        AbstractCallbacks<?, ?> parent,
        Consumer<T> onDone
    ) {
        this.builder = builder;
        this.parent = parent;
        this.onDone = Objects.requireNonNull(onDone, "onDone");
    }

    @Override
    public final AbstractCallbacks<?, ?> objectStarted() {
        return currentField == null
            ? this
            : Optional.ofNullable(objectFields.get(currentField))
                .map(Supplier::get)
                .orElseGet(this::fail);
    }

    @Override
    public final AbstractCallbacks<?, T> field(String name) {
        currentField = name;
        return this;
    }

    @Override
    public AbstractCallbacks<?, ?> objectEnded() {
        onDone.accept(builder.get());
        return parent == null ? this : parent;
    }

    @Override
    public final AbstractCallbacks<?, ?> string(String string) {
        if (currentField == null) {
            return fail();
        }
        BiConsumer<B, String> consumer = strings.get(currentField);
        if (consumer != null) {
            consumer.accept(builder, string);
        }
        return this;
    }

    @Override
    public final <N extends Number> AbstractCallbacks<?, ?> number(N number) {
        BiConsumer<B, Number> consumer = numberConsumer();
        if (consumer != null) {
            consumer.accept(builder, number);
        }
        return this;
    }

    @Override
    public final AbstractCallbacks<?, ?> truth(boolean truth) {
        BiConsumer<B, Boolean> consumer = truths.get(currentField);
        if (consumer != null) {
            consumer.accept(builder, truth);
        }
        return this;
    }

    protected B builder() {
        return builder;
    }

    protected final void onObject(
        String name,
        Supplier<AbstractCallbacks<?, ?>> nested
    ) {
        objectFields.put(name, nested);
    }

    protected final <E> void onEnum(
        String name,
        Function<String, E> enumType,
        BiConsumer<B, E> setter
    ) {
        strings.put(name, (builder, str) ->
            setter.accept(builder, enumType.apply(str)));
    }

    protected final void onString(
        String name,
        BiConsumer<B, String> setter
    ) {
        strings.put(name, setter);
    }

    protected final void onCharacter(
        String name,
        BiConsumer<B, Character> setter
    ) {
        strings.put(name, (builder, string) ->
            setter.accept(builder, toChar(string)));
    }

    protected final void onBoolean(
        String name,
        BiConsumer<B, Boolean> setter
    ) {
        truths.put(name, setter);
    }

    protected final void onFloat(
        String name,
        BiConsumer<B, Float> setter
    ) {
        numbers.put(name, (B builder, Double d) ->
            setter.accept(builder, d.floatValue()));
    }

    protected final void onDouble(
        String name,
        BiConsumer<B, Double> setter
    ) {
        numbers.put(name, setter);
    }

    protected final void onInteger(
        String name,
        BiConsumer<B, Integer> setter
    ) {
        numbers.put(name, (B builder, Long l) ->
            setter.accept(builder, l.intValue()));
    }

    protected final void onLong(
        String name,
        BiConsumer<B, Long> setter
    ) {
        numbers.put(name, setter);
    }

    protected final void onBigInteger(
        String name,
        BiConsumer<B, Long> setter
    ) {
        numbers.put(name, (B builder, BigInteger bi) ->
            setter.accept(builder, bi.longValue()));
    }

    protected final void onBigDecimal(
        String name,
        BiConsumer<B, BigDecimal> setter
    ) {
        strings.put(name, (B builder, String l) ->
            setter.accept(builder, new BigDecimal(l)));
    }

    protected final void onShort(
        String name,
        BiConsumer<B, Short> setter
    ) {
        numbers.put(name, (B builder, Long l) ->
            setter.accept(builder, l.shortValue()));
    }

    protected final void onByte(
        String name,
        BiConsumer<B, Byte> setter
    ) {
        numbers.put(name, (B builder, Long l) ->
            setter.accept(builder, l.byteValue()));
    }

    @SuppressWarnings("unchecked")
    private <N extends Number> BiConsumer<B, N> numberConsumer() {
        return (BiConsumer<B, N>) numbers.get(currentField);
    }

    private <R> R fail() {
        throw new IllegalStateException(
            "Unexpected object value for " + currentField);
    }

    private <R> R fail(Object value) {
        throw new IllegalStateException(
            "Unexpected value for " + currentField + ": " + value);
    }

    private static Character toChar(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        if (string.length() != 1) {
            return string.charAt(0);
        }
        throw new IllegalStateException("Not a char: `" + string + "`'");
    }
}
