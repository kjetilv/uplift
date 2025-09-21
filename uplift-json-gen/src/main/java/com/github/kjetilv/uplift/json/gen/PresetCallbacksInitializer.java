package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;
import module uplift.json;
import module uplift.json.gen;
import module uplift.uuid;

import static java.nio.charset.StandardCharsets.UTF_8;

@SuppressWarnings({"SameParameterValue", "unused"})
public final class PresetCallbacksInitializer<B extends Supplier<T>, T extends Record> {

    private Map<Token.Field, BiConsumer<B, ? extends Number>> numbers = new LinkedHashMap<>();

    private Map<Token.Field, BiConsumer<B, String>> strings = new LinkedHashMap<>();

    private Map<Token.Field, BiConsumer<B, Boolean>> booleans = new LinkedHashMap<>();

    private Map<Token.Field, BiFunction<Callbacks, B, Callbacks>> objects = new LinkedHashMap<>();

    private final List<PresetCallbacksInitializer<?, ?>> subs = new ArrayList<>();

    private TokenResolver tokenTrie;

    public void sub(PresetCallbacksInitializer<?, ?> sub) {
        subs.add(sub);
    }

    public Stream<Token.Field> fields() {
        return Stream.concat(
                Stream.of(numbers, strings, booleans, objects)
                    .map(Map::keySet)
                    .flatMap(Set::stream),
                subs.stream()
                    .flatMap(PresetCallbacksInitializer::fields)
            )
            .distinct()
            .sorted(Comparator.comparing(Token.Field::value));
    }

    public void onObject(String name, BiFunction<Callbacks, B, Callbacks> nested) {
        objects.put(new Token.Field(name.getBytes(UTF_8)), nested);
    }

    public void onString(String name, BiConsumer<B, String> set) {
        strings.put(new Token.Field(name.getBytes(UTF_8)), set);
    }

    public void onCharacter(String name, BiConsumer<B, Character> set) {
        strings.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, string) ->
                build(builder, set, toChar(string))
        );
    }

    public void onBoolean(String name, BiConsumer<B, Boolean> set) {
        booleans.put(new Token.Field(name.getBytes(UTF_8)), set);
    }

    public void onFloat(String name, BiConsumer<B, Float> set) {
        numbers.put(
            new Token.Field(name.getBytes(UTF_8)),
            (B builder, Double d) ->
                build(builder, set, d.floatValue())
        );
    }

    public void onDouble(String name, BiConsumer<B, Double> set) {
        numbers.put(new Token.Field(name.getBytes(UTF_8)), set);
    }

    public void onInteger(String name, BiConsumer<B, Integer> set) {
        numbers.put(
            new Token.Field(name.getBytes(UTF_8)),
            (B builder, Long l) ->
                build(builder, set, l.intValue())
        );
    }

    public void onLong(String name, BiConsumer<B, Long> set) {
        numbers.put(new Token.Field(name.getBytes(UTF_8)), set);
    }

    public void onBigInteger(String name, BiConsumer<B, BigInteger> set) {
        numbers.put(
            new Token.Field(name.getBytes(UTF_8)),
            (B builder, Long value) ->
                build(builder, set, BigInteger.valueOf(value))
        );
    }

    public void onUUID(String name, BiConsumer<B, UUID> set) {
        strings.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, str) ->
                build(builder, set, UUID.fromString(str))
        );
    }

    public void onURI(String name, BiConsumer<B, URI> set) {
        strings.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, str) ->
                build(builder, set, uri(str))
        );
    }

    public void onURL(String name, BiConsumer<B, URL> set) {
        strings.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, str) -> {
                try {
                    build(builder, set, uri(str).toURL());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Not a URL: " + str, e);
                }
            }
        );
    }

    public void onDuration(String name, BiConsumer<B, Duration> set) {
        strings.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, str) ->
                build(builder, set, Duration.parse(str))
        );
    }

    public void onLocalDateTime(String name, BiConsumer<B, LocalDateTime> set) {
        strings.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, str) ->
                build(builder, set, LocalDateTime.parse(str))
        );
    }

    public void onLocalDate(String name, BiConsumer<B, LocalDate> set) {
        strings.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, str) ->
                build(builder, set, LocalDate.parse(str))
        );
    }

    public void onOffsetDateTime(String name, BiConsumer<B, OffsetDateTime> set) {
        strings.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, str) ->
                build(builder, set, OffsetDateTime.parse(str))
        );
    }

    public void onUuid(String name, BiConsumer<B, Uuid> set) {
        strings.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, str) ->
                build(builder, set, Uuid.from(str))
        );
    }

    public void onInstant(String name, BiConsumer<B, Instant> set) {
        numbers.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, num) ->
                build(builder, set, Instant.ofEpochMilli(num.longValue()))
        );
    }

    public void onBigDecimal(String name, BiConsumer<B, BigDecimal> set) {
        strings.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, string) ->
                build(builder, set, new BigDecimal(string))
        );
        numbers.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, number) ->
                build(builder, set, BigDecimal.valueOf(number.doubleValue()))
        );
    }

    public void onShort(String name, BiConsumer<B, Short> set) {
        numbers.put(
            new Token.Field(name.getBytes(UTF_8)),
            (B builder, Long l) ->
                build(builder, set, l.shortValue())
        );
    }

    public void onByte(String name, BiConsumer<B, Byte> set) {
        numbers.put(
            new Token.Field(name.getBytes(UTF_8)),
            (B builder, Long l) ->
                build(builder, set, l.byteValue())
        );
    }

    public <E> void onEnum(
        String name,
        Function<String, E> enumType,
        BiConsumer<B, E> setter
    ) {
        strings.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, str) ->
                setter.accept(builder, enumType.apply(str))
        );
    }

    public Map<Token.Field, BiConsumer<B, ? extends Number>> getNumbers() {
        return numbers;
    }

    public Map<Token.Field, BiConsumer<B, String>> getStrings() {
        return strings;
    }

    public Map<Token.Field, BiConsumer<B, Boolean>> getBooleans() {
        return booleans;
    }

    public Map<Token.Field, BiFunction<Callbacks, B, Callbacks>> getObjects() {
        return objects;
    }

    public TokenResolver getTokenTrie() {
        return tokenTrie;
    }

    public void buildTokens(TokenResolver tokenResolver) {
        this.tokenTrie = tokenResolver != null ? tokenResolver : newTokenTrie();
        numbers = replace(numbers, this.tokenTrie);
        strings = replace(strings, this.tokenTrie);
        objects = replace(objects, this.tokenTrie);
        booleans = replace(booleans, this.tokenTrie);
        for (PresetCallbacksInitializer<?, ?> sub : subs) {
            sub.buildTokens(this.tokenTrie);
        }
    }

    private TokenTrie newTokenTrie() {
        return new TokenTrie(fields().toList());
    }

    private <V> Map<Token.Field, V> replace(
        Map<Token.Field, V> map,
        TokenResolver tokenResolver
    ) {
        return map.keySet()
            .stream()
            .map(token ->
                canonical(tokenResolver, token))
            .collect(Collectors.toMap(
                Function.identity(),
                map::get,
                (v1, v2) -> {
                    throw new IllegalStateException("No conbime: " + v1 + " / " + v2);
                },
                IdentityHashMap::new
            ));
    }

    private <V, S extends V> void build(B builder, BiConsumer<B, V> consumer, S s) {
        try {
            consumer.accept(builder, s);
        } catch (Exception e) {
            throw new IllegalStateException(
                this + ": Failed to set " + s + (s == null ? "" : " of " + s.getClass()),
                e
            );
        }
    }

    private static Token.Field canonical(TokenResolver tokenResolver, Token.Field token) {
        Token.Field resolve = tokenResolver.get(token);
        if (resolve == null) {
            throw new RuntimeException(
                "Failed to resolve token " + token + " in " + tokenResolver);
        }
        return resolve;
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

    private static URI uri(String str) {
        if (str.indexOf('\\') == -1) {
            return URI.create(str);
        }
        return URI.create(str.replaceAll("\\\\", ""));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
               "[numbers:" + numbers.size() +
               " strings:" + strings.size() +
               " booleans:" + booleans.size() +
               " objects:" + objects.size() +
               "]";
    }
}
