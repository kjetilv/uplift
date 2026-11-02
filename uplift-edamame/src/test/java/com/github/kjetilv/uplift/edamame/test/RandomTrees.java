package com.github.kjetilv.uplift.edamame.test;

import module java.base;

import static org.assertj.core.api.Assertions.assertThat;

/// Random JSON-like trees for property tests, and the expected stored form of them.
///
/// Keys and leaves come from small pools, so that trees share many equal parts. Strings are new instances
/// each time, so that sharing can be checked by identity.
final class RandomTrees {

    /// @param random Random source
    /// @param depth  Maximum depth of nested maps and lists
    /// @return Random map, with values of all supported kinds
    @SuppressWarnings("StringOperationCanBeSimplified")
    static Map<String, Object> map(Random random, int depth) {
        var keys = new ArrayList<>(KEYS);
        Collections.shuffle(keys, random);
        var map = new LinkedHashMap<String, Object>();
        keys.subList(0, random.nextInt(6))
            .forEach(key -> map.put(new String(key), value(random, depth - 1)));
        return map;
    }

    /// @param value Input tree
    /// @return The tree as it is expected to be stored: map entries with empty map or list values are left out
    static Object expected(Object value) {
        return switch (value) {
            case Map<?, ?> map -> {
                var expected = new LinkedHashMap<>();
                map.forEach((key, val) -> {
                    if (!isEmpty(val)) {
                        expected.put(key, expected(val));
                    }
                });
                yield expected;
            }
            case List<?> list -> {
                var expected = new ArrayList<>();
                list.forEach(element -> expected.add(expected(element)));
                yield expected;
            }
            case null -> null;
            default -> value;
        };
    }

    /// @param value  Input tree
    /// @param random Random source
    /// @return Equal tree with new map and string instances, and maps filled in a different order
    static Object shuffledCopy(Object value, Random random) {
        return switch (value) {
            case Map<?, ?> map -> {
                var entries = new ArrayList<Map.Entry<?, ?>>(map.entrySet());
                Collections.shuffle(entries, random);
                var copy = new LinkedHashMap<Object, Object>();
                entries.forEach(entry ->
                    copy.put(copyLeaf(entry.getKey()), shuffledCopy(entry.getValue(), random)));
                yield copy;
            }
            case List<?> list -> {
                var copy = new ArrayList<>();
                list.forEach(element -> copy.add(shuffledCopy(element, random)));
                yield copy;
            }
            case null -> null;
            default -> copyLeaf(value);
        };
    }

    /// Checks that all equal parts of the stored values, at any depth, are the same instance. Keys and values
    /// are checked separately, since they are canonicalized separately: a key `"x"` and a value `"x"` may be
    /// different instances.
    ///
    /// @param stored Stored values
    static void assertEqualPartsShared(Collection<?> stored) {
        var seenKeys = new HashMap<Object, Object>();
        var seenValues = new HashMap<Object, Object>();
        stored.forEach(value -> assertShared(value, seenKeys, seenValues));
    }

    private RandomTrees() {
    }

    private static final List<String> KEYS = List.of(
        "id", "name", "type", "tags", "owner", "status", "items", "meta", "", "a", "b", "c"
    );

    private static final List<Supplier<Object>> LEAVES = List.of(
        () -> new String("x"),
        () -> new String("y"),
        () -> new String(""),
        () -> 1L,
        () -> 1000L,
        () -> 7,
        () -> 1.5d,
        () -> -0.0d,
        () -> true,
        () -> false,
        () -> new BigDecimal("1.50"),
        () -> new BigDecimal("1.5"),
        () -> new BigInteger("123456789012345678901234567890"),
        () -> UUID.fromString("6f1c2d3e-4a5b-4c6d-8e7f-001122334455"),
        () -> Instant.parse("2024-01-01T12:00:00.000000001Z"),
        () -> LocalDate.of(2024, 2, 29),
        () -> null
    );

    private static Object value(Random random, int depth) {
        var choice = random.nextInt(depth > 0 ? 10 : 6);
        return switch (choice) {
            case 6, 7 -> map(random, depth);
            case 8, 9 -> list(random, depth);
            default -> LEAVES.get(random.nextInt(LEAVES.size())).get();
        };
    }

    private static List<Object> list(Random random, int depth) {
        var list = new ArrayList<>();
        IntStream.range(0, random.nextInt(4))
            .forEach(_ -> list.add(value(random, depth - 1)));
        return list;
    }

    private static boolean isEmpty(Object value) {
        return value instanceof Map<?, ?> map && map.isEmpty() ||
               value instanceof List<?> list && list.isEmpty();
    }

    private static Object copyLeaf(Object leaf) {
        return leaf instanceof String string ? new String(string) : leaf;
    }

    private static void assertShared(Object value, Map<Object, Object> seenKeys, Map<Object, Object> seenValues) {
        if (value == null) {
            return;
        }
        assertSame(value, seenValues, "value");
        switch (value) {
            case Map<?, ?> map -> map.forEach((key, val) -> {
                assertSame(key, seenKeys, "key");
                assertShared(val, seenKeys, seenValues);
            });
            case List<?> list -> list.forEach(element -> assertShared(element, seenKeys, seenValues));
            default -> {
            }
        }
    }

    private static void assertSame(Object value, Map<Object, Object> seen, String role) {
        var first = seen.putIfAbsent(value, value);
        if (first != null) {
            assertThat(value).as("shared %s instance of '%s'", role, value).isSameAs(first);
        }
    }
}
