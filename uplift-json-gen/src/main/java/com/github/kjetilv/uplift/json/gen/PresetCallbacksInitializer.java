package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;
import com.github.kjetilv.uplift.json.TokenResolver;
import com.github.kjetilv.uplift.json.gen.tokens.TokenTable;

import static java.nio.charset.StandardCharsets.UTF_8;

@SuppressWarnings({"SameParameterValue", "unused"})
public final class PresetCallbacksInitializer<B extends Supplier<T>, T extends Record> {

    private final Class<?> host;

    private Map<Token.Field, BiConsumer<B, ? extends Number>> numbers = new LinkedHashMap<>();

    private Map<Token.Field, BiConsumer<B, String>> strings = new LinkedHashMap<>();

    private Map<Token.Field, BiConsumer<B, Boolean>> booleans = new LinkedHashMap<>();

    private Map<Token.Field, BiFunction<Callbacks, B, Callbacks>> objects = new LinkedHashMap<>();

    private final List<PresetCallbacksInitializer<?, ?>> subs = new ArrayList<>();

    private TokenResolver tokenResolver;

    public PresetCallbacksInitializer(Class<?> host) {
        this.host = Objects.requireNonNull(host, "host");
    }

    public void sub(PresetCallbacksInitializer<?, ?> sub) {
        subs.add(sub);
    }

    public Stream<Token.Field> fields() {
        return fields(new HashSet<>())
            .stream()
            .distinct()
            .sorted(
                Comparator.comparing(Token.Field::value)
            );
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
            (builder, value) ->
                build(builder, set, value.floatValue())
        );
    }

    public void onDouble(String name, BiConsumer<B, Double> set) {
        numbers.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, value) ->
                build(builder, set, value.doubleValue())
            );
    }

    public void onInteger(String name, BiConsumer<B, Integer> set) {
        numbers.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, value) ->
                build(builder, set, (int) exact(value, Integer.MIN_VALUE, Integer.MAX_VALUE, "int"))
        );
    }

    public void onLong(String name, BiConsumer<B, Long> set) {
        numbers.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, value) ->
                build(builder, set, exact(value, Long.MIN_VALUE, Long.MAX_VALUE, "long"))
            );
    }

    public void onBigInteger(String name, BiConsumer<B, BigInteger> set) {
        numbers.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, value) ->
                build(builder, set, bigInteger(value))
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

    public void onInstant(String name, BiConsumer<B, Instant> set) {
        numbers.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, num) ->
                build(builder, set, Instant.ofEpochMilli(exact(num, Long.MIN_VALUE, Long.MAX_VALUE, "long")))
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
                build(builder, set, bigDecimal(number))
        );
    }

    public void onShort(String name, BiConsumer<B, Short> set) {
        numbers.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, value) ->
                build(builder, set, (short) exact(value, Short.MIN_VALUE, Short.MAX_VALUE, "short"))
        );
    }

    public void onByte(String name, BiConsumer<B, Byte> set) {
        numbers.put(
            new Token.Field(name.getBytes(UTF_8)),
            (builder, value) ->
                build(builder, set, (byte) exact(value, Byte.MIN_VALUE, Byte.MAX_VALUE, "byte"))
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

    public TokenResolver getTokenResolver() {
        return tokenResolver;
    }

    public void buildTokens() {
        buildTokens(null, null);
    }

    public void buildTokens(TokenResolver tokenResolver, Set<PresetCallbacksInitializer<?, ?>> handled) {
        if (handled != null && handled.contains(this)) {
            return;
        }
        this.tokenResolver = tokenResolver != null ? tokenResolver : newTokenTable();
        this.numbers = replace(this.numbers, this.tokenResolver);
        this.strings = replace(this.strings, this.tokenResolver);
        this.objects = replace(this.objects, this.tokenResolver);
        this.booleans = replace(this.booleans, this.tokenResolver);
        var newHandled = Stream.concat(
                Stream.ofNullable(handled).flatMap(Set::stream),
                Stream.of(this)
            )
            .collect(Collectors.toSet());
        for (var sub : subs) {
            sub.buildTokens(this.tokenResolver, newHandled);
        }
    }

    private List<Token.Field> fields(Collection<Class<?>> known) {
        if (known.contains(host)) {
            return List.of();
        }
        var primitiveFields = Stream.of(numbers, strings, booleans, objects)
            .map(Map::keySet)
            .flatMap(Set::stream)
            .toList();
        Collection<Class<?>> expandedKnown = Stream.concat(
                known.stream(),
                Stream.of(this.host)
            )
            .collect(Collectors.toSet());
        var subFields = subs.stream()
            .map(presetCallbacksInitializer ->
                presetCallbacksInitializer.fields(expandedKnown))
            .flatMap(Collection::stream)
            .toList();
        return Stream.of(primitiveFields, subFields)
            .flatMap(Collection::stream)
            .toList();
    }

    private TokenTable newTokenTable() {
        return new TokenTable(fields().toList());
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

    private static final Pattern BACKSLASHES = Pattern.compile("\\\\");

    private static Token.Field canonical(TokenResolver tokenResolver, Token.Field token) {
        var resolve = tokenResolver.get(token);
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
        if (string.length() == 1) {
            return string.charAt(0);
        }
        throw new IllegalStateException("Not a char: `" + string + "`'");
    }

    private static URI uri(String str) {
        return str.indexOf('\\') == -1
            ? URI.create(str)
            : URI.create(BACKSLASHES.matcher(str).replaceAll(""));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + host.getSimpleName() +
               " num:" + numbers.size() +
               " str:" + strings.size() +
               " boo:" + booleans.size() +
               " obj:" + objects.size() +
               " subs:" + subs.size() +
               "]";
    }

    /// Exact conversion. The parser gives [Long], [BigInteger] or [BigDecimal].
    ///
    /// @throws ArithmeticException If the number has a fractional part
    private static BigInteger bigInteger(Number number) {
        return switch (number) {
            case BigInteger bigInteger -> bigInteger;
            case BigDecimal bigDecimal -> bigDecimal.toBigIntegerExact();
            case Long l -> BigInteger.valueOf(l);
            case Integer i -> BigInteger.valueOf(i);
            default -> new BigDecimal(number.toString()).toBigIntegerExact();
        };
    }

    /// Exact conversion. The parser gives [Long], [BigInteger] or [BigDecimal].
    private static BigDecimal bigDecimal(Number number) {
        return switch (number) {
            case BigDecimal bigDecimal -> bigDecimal;
            case BigInteger bigInteger -> new BigDecimal(bigInteger);
            case Long l -> BigDecimal.valueOf(l);
            case Integer i -> BigDecimal.valueOf(i);
            default -> new BigDecimal(number.toString());
        };
    }

    /// Exact conversion to an integral type. A number with a fractional part, or outside the range, is an error.
    ///
    /// @param number Number from the parser: [Long], [BigInteger] or [BigDecimal]
    /// @param min    Smallest allowed value
    /// @param max    Largest allowed value
    /// @param type   Name of the target type, for the message
    /// @return The value
    /// @throws ArithmeticException If the number does not fit
    private static long exact(Number number, long min, long max, String type) {
        try {
            var value = switch (number) {
                case Long l -> l;
                case Integer i -> (long) i;
                case Short s -> (long) s;
                case Byte b -> (long) b;
                case BigInteger bigInteger -> bigInteger.longValueExact();
                case BigDecimal bigDecimal -> bigDecimal.longValueExact();
                default -> new BigDecimal(number.toString()).longValueExact();
            };
            if (value < min || value > max) {
                throw new ArithmeticException("out of range");
            }
            return value;
        } catch (ArithmeticException e) {
            throw new ArithmeticException("Value " + number + " does not fit in " + type);
        }
    }
}
