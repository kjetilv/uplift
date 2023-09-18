package com.github.kjetilv.uplift.json;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractCallbacks<T> implements Events.Callbacks<AbstractCallbacks<?>> {

    private final AbstractCallbacks<?> parent;

    private final Consumer<T> onDone;

    private final Map<String, Consumer<? extends Number>> numbers = new LinkedHashMap<>();

    private final Map<String, Consumer<String>> strings = new LinkedHashMap<>();

    private final Map<String, Consumer<Boolean>> truths = new LinkedHashMap<>();

    private final Map<String, Supplier<AbstractCallbacks<?>>> objectFields = new LinkedHashMap<>();

    private String currentField;

    private Supplier<T> supplier;

    protected AbstractCallbacks(AbstractCallbacks<?> parent, Consumer<T> onDone) {
        this.parent = parent;
        this.onDone = Objects.requireNonNull(onDone, "onDone");
    }

    @Override
    public final AbstractCallbacks<?> objectStarted() {
        return currentField == null
            ? this
            : Optional.ofNullable(objectFields.get(currentField))
                .map(Supplier::get)
                .orElseGet(this::fail);
    }

    @Override
    public final AbstractCallbacks<T> field(String name) {
        currentField = name;
        return this;
    }

    @Override
    public AbstractCallbacks<?> objectEnded() {
        onDone.accept(supplier.get());
        return parent == null ? this : parent;
    }

    @Override
    public final AbstractCallbacks<?> string(String string) {
        if (currentField == null) {
            return fail();
        }
        Consumer<String> consumer = strings.get(currentField);
        if (consumer != null) {
            consumer.accept(string);
        }
        return this;
    }

    @Override
    public final <N extends Number> AbstractCallbacks<?> number(N number) {
        Consumer<N> consumer = numberConsumer();
        if (consumer != null) {
            consumer.accept(number);
        }
        return this;
    }

    @Override
    public final AbstractCallbacks<?> truth(boolean truth) {
        Consumer<Boolean> consumer = truths.get(currentField);
        if (consumer != null) {
            consumer.accept(truth);
        }
        return this;
    }

    protected final void get(Supplier<T> get) {
        this.supplier = get;
    }

    protected final void onObject(String name, Supplier<AbstractCallbacks<?>> nested) {
        objectFields.put(name, nested);
    }

    protected final <E> void onTypedString(String name, Function<String, E> enumType, Consumer<E> setter) {
        strings.put(name, str -> setter.accept(enumType.apply(str)));
    }

    protected final void onString(String name, Consumer<String> setter) {
        strings.put(name, setter);
    }

    protected final void onTruth(String name, Consumer<Boolean> setter) {
        truths.put(name, setter);
    }

    protected final void onFloat(String name, Consumer<Float> setter) {
        numbers.put(name, (Double d) -> setter.accept(d.floatValue()));
    }

    protected final void onDouble(String name, Consumer<Double> setter) {
        numbers.put(name, setter);
    }

    protected final void onInteger(String name, Consumer<Integer> setter) {
        numbers.put(name, (Long l) -> setter.accept(Math.toIntExact(l)));
    }

    protected final void onShort(String name, Consumer<Short> setter) {
        numbers.put(name, (Long l) -> setter.accept(l.shortValue()));
    }

    protected final void onByte(String name, Consumer<Byte> setter) {
        numbers.put(name, (Long l) -> setter.accept(l.byteValue()));
    }

    @SuppressWarnings("unchecked")
    private <N extends Number> Consumer<N> numberConsumer() {
        return (Consumer<N>) numbers.get(currentField);
    }

    private final <R> R fail() {
        throw new IllegalStateException("Unexpected object value for " + currentField);
    }

    private final <R> R fail(Object value) {
        throw new IllegalStateException("Unexpected value for " + currentField + ": " + value);
    }
}
