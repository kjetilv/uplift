package com.github.kjetilv.uplift.json.gen;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.NullCallbacks;
import com.github.kjetilv.uplift.json.Token;
import com.github.kjetilv.uplift.uuid.Uuid;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings({"SameParameterValue", "unused"})
public abstract class AbstractCallbacks<B extends Supplier<T>, T extends Record> implements Callbacks {

    private final B builder;

    private final AbstractCallbacks<?, ?> parent;

    private final Consumer<T> onDone;

    private final Map<Token, BiConsumer<B, ? extends Number>> numbers = new LinkedHashMap<>();

    private final Map<Token, BiConsumer<B, String>> strings = new LinkedHashMap<>();

    private final Map<Token, BiConsumer<B, Boolean>> booleans = new LinkedHashMap<>();

    private final Map<Token, Supplier<Callbacks>> objectFields = new LinkedHashMap<>();

    private Token currentField;

    protected AbstractCallbacks(B builder, AbstractCallbacks<?, ?> parent, Consumer<T> onDone) {
        this.builder = Objects.requireNonNull(builder, "builder");
        this.parent = parent;
        this.onDone = Objects.requireNonNull(onDone, "onDone");
    }

    @Override
    public final Callbacks objectStarted() {
        return currentField == null
            ? this
            : Optional.ofNullable(objectFields.get(currentField))
                .map(Supplier::get)
                .orElseGet(() ->
                    new NullCallbacks(this));
    }

    @Override
    public final Callbacks field(Token token) {
        currentField = token;
        return this;
    }

    @Override
    public final Callbacks objectEnded() {
        onDone.accept(builder.get());
        return parent == null ? this : parent;
    }

    @Override
    public final Callbacks string(String token) {
        if (currentField == null) {
            return fail();
        }
        BiConsumer<B, String> consumer = strings.get(currentField);
        if (consumer != null) {
            build(consumer, token);
        }
        return this;
    }

    @Override
    public final <N extends Number> Callbacks number(N number) {
        BiConsumer<B, Number> consumer = numberConsumer();
        if (consumer != null) {
            build(consumer, number);
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
    public Collection<Token> canonicalTokens() {
        return Stream.of(
                numbers,
                strings,
                booleans,
                objectFields
            )
            .map(Map::keySet).flatMap(Set::stream).distinct()
            .toList();
    }

    protected B builder() {
        return builder;
    }

    protected final void onObject(
        String name,
        Supplier<Callbacks> nested
    ) {
        objectFields.put(new Token(name), nested);
    }

    protected final <E> void onEnum(
        String name,
        Function<String, E> enumType,
        BiConsumer<B, E> setter
    ) {
        strings.put(
            new Token(name),
            (builder, str) ->
                setter.accept(builder, enumType.apply(str))
        );
    }

    protected final void onString(String name, BiConsumer<B, String> set) {
        strings.put(new Token(name), set);
    }

    protected final void onCharacter(String name, BiConsumer<B, Character> set) {
        strings.put(
            new Token(name),
            (builder, string) ->
                build(set, toChar(string))
        );
    }

    protected final void onBoolean(String name, BiConsumer<B, Boolean> set) {
        booleans.put(new Token(name), set);
    }

    protected final void onFloat(String name, BiConsumer<B, Float> set) {
        numbers.put(
            new Token(name),
            (B builder, Double d) ->
                build(set, d.floatValue())
        );
    }

    protected final void onDouble(String name, BiConsumer<B, Double> set) {
        numbers.put(new Token(name), set);
    }

    protected final void onInteger(String name, BiConsumer<B, Integer> set) {
        numbers.put(
            new Token(name),
            (B builder, Long l) ->
                build(set, l.intValue())
        );
    }

    protected final void onLong(String name, BiConsumer<B, Long> set) {
        numbers.put(new Token(name), set);
    }

    protected final void onBigInteger(String name, BiConsumer<B, BigInteger> set) {
        numbers.put(
            new Token(name),
            (B builder, Long value) ->
                build(set, BigInteger.valueOf(value))
        );
    }

    protected final void onUUID(String name, BiConsumer<B, UUID> set) {
        strings.put(
            new Token(name),
            (builder, str) ->
                build(set, UUID.fromString(str))
        );
    }

    protected final void onURI(String name, BiConsumer<B, URI> set) {
        strings.put(
            new Token(name),
            (builder, str) ->
                build(set, URI.create(str))
        );
    }

    protected final void onURL(String name, BiConsumer<B, URL> set) {
        strings.put(
            new Token(name),
            (builder, str) -> {
                try {
                    build(set, URI.create(str).toURL());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Not a URL: " + str, e);
                }
            }
        );
    }

    protected final void onDuration(String name, BiConsumer<B, Duration> set) {
        strings.put(
            new Token(name),
            (builder, str) ->
                build(set, Duration.parse(str))
        );
    }

    protected final void onLocalDateTime(String name, BiConsumer<B, LocalDateTime> set) {
        strings.put(
            new Token(name),
            (builder, str) ->
                build(set, LocalDateTime.parse(str))
        );
    }

    protected final void onLocalDate(String name, BiConsumer<B, LocalDate> set) {
        strings.put(
            new Token(name),
            (builder, str) ->
                build(set, LocalDate.parse(str))
        );
    }

    protected final void onOffsetDateTime(String name, BiConsumer<B, OffsetDateTime> set) {
        strings.put(
            new Token(name),
            (builder, str) ->
                build(set, OffsetDateTime.parse(str))
        );
    }

    protected final void onUuid(String name, BiConsumer<B, Uuid> set) {
        strings.put(
            new Token(name),
            (builder, str) ->
                build(set, Uuid.from(str))
        );
    }

    protected final void onInstant(String name, BiConsumer<B, Instant> set) {
        numbers.put(
            new Token(name),
            (builder, num) ->
                build(set, Instant.ofEpochMilli(num.longValue()))
        );
    }

    protected final void onBigDecimal(String name, BiConsumer<B, BigDecimal> set) {
        strings.put(
            new Token(name),
            (builder, string) ->
                build(set, new BigDecimal(string))
        );
        numbers.put(
            new Token(name),
            (builder, number) ->
                build(set, BigDecimal.valueOf(number.doubleValue()))
        );
    }

    protected final void onShort(String name, BiConsumer<B, Short> set) {
        numbers.put(
            new Token(name),
            (B builder, Long l) ->
                build(set, l.shortValue())
        );
    }

    protected final void onByte(String name, BiConsumer<B, Byte> set) {
        numbers.put(
            new Token(name),
            (B builder, Long l) ->
                build(set, l.byteValue())
        );
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
        throw new IllegalStateException(
            "Unexpected object value for `" + currentField + '`');
    }

    private <R> R fail(Object value) {
        throw new IllegalStateException(
            "Unexpected value for `" + currentField + "`: " + value);
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + builder + " -> " + parent + "]";
    }
}
