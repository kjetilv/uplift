package com.github.kjetilv.uplift.json.gens;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.kjetilv.uplift.json.Events;

public abstract class AbstractCallbacks<T> implements Events.Callbacks<AbstractCallbacks<?>>, Supplier<T> {

    private final AbstractCallbacks<?> parent;

    private final Consumer<T> onDone;

    private final Map<String, Consumer<? extends Number>> numbers = new LinkedHashMap<>();

    private final Map<String, Consumer<String>> strings = new LinkedHashMap<>();

    private final Map<String, Consumer<Boolean>> truths = new LinkedHashMap<>();

    private final Map<String, Supplier<AbstractCallbacks<?>>> objectFields = new LinkedHashMap<>();

    private String currentField;

    private Supplier<T> get;

    protected AbstractCallbacks(AbstractCallbacks<?> parent, Consumer<T> onDone) {
        this.parent = parent;
        this.onDone = onDone;
    }

    @Override
    public final AbstractCallbacks<?> objectStarted() {
        if (currentField == null) {
            return this;
        }
        Supplier<AbstractCallbacks<?>> nester = objectFields.get(currentField);
        return nester == null ? fail() : nester.get();
    }

    @Override
    public final AbstractCallbacks<T> field(String name) {
        try {
            return this;
        } finally {
            currentField = name;
        }
    }

    @Override
    public AbstractCallbacks<?> objectEnded() {
        try {
            return parent == null ? this : parent;
        } finally {
            onDone.accept(get());
        }
    }

    @Override
    public final AbstractCallbacks<?> string(String string) {
        Consumer<String> consumer = strings.get(currentField);
        if (consumer == null) {
            return fail(string);
        }
        try {
            return this;
        } finally {
            consumer.accept(string);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final AbstractCallbacks<?> number(Number number) {
        Consumer<Number> consumer = (Consumer<Number>) numbers.get(currentField);
        if (consumer == null) {
            return fail(number);
        }
        try {
            return this;
        } finally {
            consumer.accept(number);
        }
    }

    @Override
    public final AbstractCallbacks<?> truth(boolean truth) {
        Consumer<Boolean> consumer = truths.get(currentField);
        if (consumer == null) {
            return fail(truth);
        }
        try {
            return this;
        } finally {
            consumer.accept(truth);
        }
    }

    @Override
    public final T get() {
        return get.get();
    }

    protected final void get(Supplier<T> get) {
        this.get = get;
    }

    protected final void onObject(String name, Supplier<AbstractCallbacks<?>> nested) {
        objectFields.put(name, nested);
    }

    protected final <E extends Enum<?>> void onEnum(String name, Function<String, E> enumType, Consumer<E> setter) {
        strings.put(name, str -> setter.accept(enumType.apply(str)));
    }

    protected final void onString(String name, Consumer<String> setter) {
        strings.put(name, setter);
    }

    protected final void onTruth(String name, Consumer<Boolean> setter) {
        truths.put(name, setter);
    }

    protected final void onFloat(String name, Consumer<Float> setter) {
        numbers.put(name, setter);
    }

    protected final void onDouble(String name, Consumer<Double> setter) {
        numbers.put(name, setter);
    }

    protected final void onInteger(String name, Consumer<Integer> setter) {
        numbers.put(name, (Long l) -> setter.accept(l.intValue()));
    }

    private final <R> R fail() {
        throw new IllegalStateException("Unexpected object value for " + currentField);
    }

    private final <R> R fail(Object value) {
        throw new IllegalStateException("Unexpected value for " + currentField + ": " + value);
    }
}