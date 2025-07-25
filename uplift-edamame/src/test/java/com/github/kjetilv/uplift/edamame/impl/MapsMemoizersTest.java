package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.*;
import com.github.kjetilv.uplift.hash.Bytes;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.hash.Hashes;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.chrono.MinguoEra;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.kjetilv.uplift.edamame.impl.InternalFactory.create;
import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.*;

class MapsMemoizersTest {

    static HashBuilder<byte[], HashKind.K128> md5HashBuilder() {
        return Hashes.hashBuilder(HashKind.K128).map(Bytes::from);
    }

    static com.github.kjetilv.uplift.hash.Hash<HashKind.K128> random() {
        UUID randomUUID = UUID.randomUUID();
        return Hashes.of(
            randomUUID.getMostSignificantBits(),
            randomUUID.getLeastSignificantBits()
        );
    }

    @Test
    void shouldHandleLeafCollisions() {
        Object bd = new BigDecimal("123.234");
        Object bi = new BigInteger("424242");
        LeafHasher<HashKind.K128> leafHasher = collidingLeafHasher();

        MapsMemoizer<Long, String> mapsMemoizer = create(null, leafHasher, HashKind.K128, null);

        mapsMemoizer.put(
            42L, Map.of(
                "zot2", bd,
                "zot1", bi
            )
        );
        BigDecimal bdCopy = new BigDecimal("123.234");
        BigInteger biCopy = new BigInteger("424242");
        mapsMemoizer.put(
            43L, Map.of(
                "zot2", bdCopy,
                "zot1", biCopy
            )
        );
        MemoizedMaps<Long, String> access = mapsMemoizer.complete();

        Map<String, ?> map42 = access.get(42L);
        Map<String, ?> map43 = access.get(43L);

        Object bi42 = map42.get("zot1");
        Object bd42 = map42.get("zot2");

        Object bi43 = map43.get("zot1");
        Object bd43 = map43.get("zot2");

        assertSame(bi, bi42);
        assertEquals(bi, bi42);
        assertSame(bd, bd42);
        assertEquals(bd, bd42);

        assertNotSame(bi, bi43);
        assertEquals(bi, bi43);
        assertNotSame(bd, bd43);
        assertEquals(bd, bd43);

//        assertSame(bd, access.get(43L).get("zot1"));
//        assertSame(bi, access.get(43L).get("zot2"));
    }

    @Test
    void shouldHandlePojos() {
        Object l1 = new Leaf(42L, "123.234");
        Object l2 = new Leaf(34L, "424242");

        MapsMemoizer<Long, String> mapsMemoizer = mapsMemoizer();

        mapsMemoizer.put(
            42L, Map.of(
                "zot2", l1,
                "zot1", l2
            )
        );
        Object l1Copy = new Leaf(42L, "123.234");
        Object l2Copy = new Leaf(34L, "424242");

        mapsMemoizer.put(
            43L, Map.of(
                "zot2", l1Copy,
                "zot1", l2Copy
            )
        );
        MemoizedMaps<Long, String> access = mapsMemoizer.complete();

        Map<String, ?> map42 = access.get(42L);
        Map<String, ?> map43 = access.get(43L);

        Object bi42 = map42.get("zot1");
        Object bd42 = map42.get("zot2");

        Object bi43 = map43.get("zot1");
        Object bd43 = map43.get("zot2");

        assertSame(l2, bi42);
        assertEquals(l2, bi42);
        assertSame(l1, bd42);
        assertEquals(l1, bd42);

        assertSame(l2, bi43);
        assertEquals(l2, bi43);
        assertSame(l1, bd43);
        assertEquals(l1, bd43);

//        assertSame(bd, access.get(43L).get("zot1"));
//        assertSame(bi, access.get(43L).get("zot2"));
    }

    @Test
    void shouldHandleCollisions() {
        com.github.kjetilv.uplift.hash.Hash<HashKind.K128> collider = random();
        LeafHasher<HashKind.K128> leafHasher = leaf ->
            leaf.equals("3") || leaf.equals("7")
                ? collider
                : new DefaultLeafHasher<>(
                    MapsMemoizersTest::md5HashBuilder,
                    PojoBytes.HASHCODE
                ).hash(leaf);
        MapsMemoizer<Long, String> cache = create(null, leafHasher, HashKind.K128, null);

        for (int i = 0; i < 10; i++) {
            cache.put((long) i, Map.of("foo", String.valueOf(i)));
        }
        MemoizedMaps<Long, String> access = cache.complete();
        for (int i = 0; i < 10; i++) {
            Map<String, String> reconstructed = Map.of("foo", String.valueOf(i));
            assertEquals(reconstructed, access.get((long) i));
        }
    }

    @Test
    void shouldRespectCanonicalKeys() {
        KeyHandler<CaKe> caKeKeyHandler = s -> CaKe.get(s.toString());
        MapsMemoizer<Long, CaKe> cache = create(caKeKeyHandler, HashKind.K128);

        Map<String, Object> in42 = build42(zot1Zot2());
        Map<String, ? extends Number> hh0hh1 = hh0hh1();

        Map<String, Object> in43 = Map.of(
            "fooTop", "zot",
            "zot", zot1Zot2(),
            "a", hh0hh1
        );

        Map<String, ? extends Number> hh0hh2 = hh0hh1();
        Map<String, Object> in44 = Map.of(
            "fooTop", "zot",
            "zot", zot1Zot2(),
            "a", Map.of(
                "e1", 2,
                "2", hh0hh2
            )
        );
        Map<String, Object> in48 = build42(zot1Zot2());

        cache.put(42L, in42);
        cache.put(48L, in48);
        Map<CaKe, ?> out42 = cache.get(42L);
        Map<CaKe, ?> out42as48 = cache.get(48L);
        assertSame(
            cache.get(42L),
            out42as48,
            "Same structure should return same identity"
        );
        cache.put(43L, in43);
        cache.put(44L, in44);

        MemoizedMaps<Long, CaKe> access = cache.complete();
        Map<CaKe, ?> cake43 = access.get(43L);
        Map<CaKe, ?> cake44 = access.get(44L);

        assertSame(
            getKey(cake43, "zot"),
            getKey(cake44, "zot")
        );
        assertSame(
            getKey(cake43, "fooTop"),
            getKey(cake44, "fooTop")
        );

        assertEquals(
            hh0hh1(),
            in43.get("a")
        );
        assertEquals(
            in44.get("a"),
            Map.of(
                "e1", 2,
                "2", hh0hh1()
            )
        );

        assertEquals(
            getDeep(in44, "a", "2"),
            hh0hh1()
        );

        assertNotSame(
            in42.get("zotCopy"),
            in43.get("zot")
        );

        assertSame(
            out42.get(CaKe.get("zot")),
            cake43.get(CaKe.get("zotCopy"))
        );

        assertSame(
            cake43.get(CaKe.get("zotCopy")),
            out42.get(CaKe.get("zot"))
        );

        Map<CaKe, ?> stringMap42 = access.get(42L);
        Map<CaKe, ?> stringMap43 = access.get(43L);
        Map<CaKe, ?> stringMap44 = access.get(44L);
        Map<CaKe, ?> stringMap44a = access.get(44L);

        assertSame(stringMap44, stringMap44a);

        Object inner42 = stringMap42.get(CaKe.get("zotCopy"));
        Object inner43 = stringMap43.get(CaKe.get("zot"));
        assertSame(inner42, inner43);
    }

    @Test
    void shouldStripBlankData() {
        MapsMemoizer<Long, String> cache = mapsMemoizer();

        cache.put(
            42L,
            Map.of(
                "foo", "bar",
                "zot", Collections.emptyList(),
                "zip", Collections.emptyMap(),
                "arr", new int[0]
            )
        );

        cache.put(
            45L, Map.of(
                "foo", "bar",
                "zot", Collections.emptyList()
            )
        );
        MemoizedMaps<Long, String> access = cache.complete();

        assertEquals(
            Map.of("foo", "bar"),
            access.get(42L)
        );

        assertSame(
            access.get(42L),
            access.get(45L)
        );
    }

    @Test
    void shouldStringify() {
        MapsMemoizer<Long, String> cache = create(Object::toString, HashKind.K128, null);

        cache.put(
            42L,
            Map.of(1, "bar")
        );
        cache.put(
            45L,
            Map.of(
                1, "bar",
                true, Collections.emptyList()
            )
        );
        MemoizedMaps<Long, String> access = cache.complete();
        Map<String, ?> out42 = access.get(42L);
        Map<String, ?> out45 = access.get(45L);
        assertEquals(
            Map.of("1", "bar"),
            access.get(42L)
        );

        assertSame(
            access.get(42L),
            out45
        );
        assertSame(
            out42,
            out45
        );
        assertSame(
            out42,
            access.get(45L)
        );
    }

    @Test
    void shouldIgnoreKeyOrder() {
        MapsMemoizer<Long, String> cache = mapsMemoizer();

        cache.put(
            42L,
            map(IntStream.range(0, 10))
        );
        cache.put(
            43L,
            map(IntStream.range(0, 10)
                .map(i -> 9 - i))
        );
        MemoizedMaps<Long, String> access = cache.complete();
        Map<String, ?> canon42 = access.get(42L);
        Map<String, ?> canon43 = access.get(43L);
        assertEquals(canon42, canon43);
        assertSame(canon42, canon43);
    }

    @Test
    void shouldPreserveListOrder() {
        MapsMemoizer<Long, String> cache = mapsMemoizer();

        cache.put(
            42L,
            Map.of(
                "foo", IntStream.range(0, 10).mapToObj(String::valueOf)
                    .toList()
            )
        );
        cache.put(
            43L,
            Map.of(
                "foo", IntStream.range(0, 10)
                    .map(i -> 9 - i).mapToObj(String::valueOf)
                    .toList()
            )
        );
        MemoizedMaps<Long, String> access = cache.complete();
        Map<String, ?> canon42 = access.get(42L);
        Map<String, ?> canon43 = access.get(43L);
        assertNotEquals(canon42, canon43);
    }

    @Test
    void shouldPreserveArrayOrder() {
        MapsMemoizer<Long, String> cache = mapsMemoizer();

        cache.put(
            42L,
            Map.of(
                "foo", IntStream.range(0, 10).mapToObj(String::valueOf)
                    .toArray()
            )
        );
        cache.put(
            43L,
            Map.of(
                "foo", IntStream.range(0, 10)
                    .map(i -> 9 - i).mapToObj(String::valueOf)
                    .toArray()
            )
        );
        MemoizedMaps<Long, String> access = cache.complete();
        Map<String, ?> canon42 = access.get(42L);
        Map<String, ?> canon43 = access.get(43L);
        assertNotEquals(canon42, canon43);
    }

    @SuppressWarnings({"TextBlockMigration", "unchecked", "StringOperationCanBeSimplified"})
    @Test
    void shouldGrudginglyAcceptNullsInLists() {
        MapsMemoizer<Long, String> cache = mapsMemoizer();

        List<String> canonicalList = Arrays.asList("1", null, "a");
        cache.put(
            42L,
            Map.of(
                "foo", canonicalList
            )
        );
        List<String> otherList = Arrays.asList(new String("1"), null, new String("a"));
        cache.put(
            43L,
            Map.of(
                "foo", otherList
            )
        );
        assertNotSame(otherList.get(0), canonicalList.get(0));
        assertNotSame(otherList.get(2), canonicalList.get(2));

        MemoizedMaps<Long, String> access = cache.complete();
        Map<String, ?> canon42 = access.get(42L);
        Map<String, ?> canon43 = access.get(43L);
        assertEquals(canon42, canon43);

        List<String> list42 = (List<String>) canon42.get("foo");
        List<String> list43 = (List<String>) canon43.get("foo");

        assertEquals(Arrays.asList("1", null, "a"), list42);
        assertEquals(Arrays.asList("1", null, "a"), list43);

        assertSame(list42.get(0), list43.get(0));
        assertNull(list42.get(1));
        assertNull(list43.get(1));
        assertSame(list42.get(2), list43.get(2));
    }

    @Test
    void shouldPreserveIdentities() {
        MapsMemoizer<Long, String> cache = mapsMemoizer();
        Map<String, Object> in42 = build42(zot1Zot2());
        Map<String, ? extends Number> hh0hh1 = hh0hh1();
        Map<String, Object> in43 = Map.of(
            "fooTop", "zot",
            "zot", zot1Zot2(),
            "a", hh0hh1
        );
        Map<String, ? extends Number> hh0hh2 = hh0hh1();
        Map<String, Object> in44 = Map.of(
            "fooTop", "zot",
            "zot", zot1Zot2(),
            "a", Map.of(
                "e1", 2,
                "2", hh0hh2
            )
        );

        cache.put(42L, in42);
        cache.put(48L, build42(zot1Zot2()));
        cache.put(43L, in43);
        cache.put(44L, in44);

        MemoizedMaps<Long, String> access = cache.complete();
        Map<String, ?> out42as48 = access.get(48L);
        Map<String, ?> out42 = access.get(42L);

        assertSame(
            out42,
            out42as48,
            "Same structure should return same identity"
        );
        Map<String, ?> out43 = access.get(43L);
        assertEquals(
            hh0hh1(),
            in43.get("a")
        );
        assertEquals(
            in44.get("a"),
            Map.of(
                "e1", 2,
                "2", hh0hh1()
            )
        );

        assertEquals(
            getDeep(in44, "a", "2"),
            hh0hh1()
        );

        assertNotSame(
            in42.get("zotCopy"),
            in43.get("zot")
        );

        assertSame(
            out42.get("zot"),
            out43.get("zotCopy")
        );

        assertSame(
            out43.get("zotCopy"),
            out42.get("zot")
        );

        Map<String, ?> stringMap42 = access.get(42L);
        Map<String, ?> stringMap43 = access.get(43L);
        Map<String, ?> stringMap44 = access.get(44L);
        Map<String, ?> stringMap44a = access.get(44L);

        assertSame(stringMap44, stringMap44a);

        assertSame(
            stringMap42.get("zotCopy"),
            stringMap43.get("zot")
        );
        assertEquals(
            hh0hh1(),
            getDeep(stringMap44, "a", "2")
        );
        assertSame(
            stringMap43.get("a"),
            getDeep(stringMap44, "a", "2")
        );
    }

    @Test
    void shouldCanonicalizeLeaves() {
        MapsMemoizer<Long, String> cache = mapsMemoizer();

        BigDecimal bd = new BigDecimal("123.234");
        BigInteger bi = new BigInteger("424242");

        cache.put(
            42L, Map.of(
                "zot2", bd,
                "zot1", bi
            )
        );
        cache.put(
            43L, Map.of(
                "zot2", new BigDecimal("123.234"),
                "zot1", new BigInteger("424242")
            )
        );
        MemoizedMaps<Long, String> access = cache.complete();

        assertSame(bd, access.get(42L).get("zot2"));
        assertSame(bi, access.get(42L).get("zot1"));

        assertSame(bd, access.get(43L).get("zot2"));
        assertSame(bi, access.get(43L).get("zot1"));
    }

    @Test
    void shouldHandlePrimitives() {
        Map<String, ? extends Comparable<? extends Comparable<?>>> map = Map.ofEntries(
            entry("ldt", LocalDateTime.now()),
            entry("ld", LocalDate.now()),
            entry("e", MinguoEra.BEFORE_ROC),
            entry("y", Year.of(2015)),
            entry("ym", YearMonth.of(2015, Month.JUNE)),
            entry("m", Month.APRIL),
            entry("md", MonthDay.of(6, 28)),
            entry("dow", DayOfWeek.FRIDAY),
            entry("s", (short) 4),
            entry("b", (byte) 2),
            entry("d", 2.0),
            entry("f", (float) 1.0),
            entry("i", Instant.now()),
            entry("odt", OffsetDateTime.now()),
            entry("ot", OffsetTime.now()),
            entry("uuid", UUID.randomUUID()),
            entry("zdt", Instant.now().atZone(ZoneId.systemDefault()))
        );

        MapsMemoizer<Long, String> cache = mapsMemoizer();

        cache.put(42L, map);
        assertEquals(map, cache.get(42L));
    }

    @Test
    void shouldPutIfAbsent() {
        MapsMemoizer<Long, String> cache = mapsMemoizer();

        Map<String, String> foo = Map.of("foo", "bar");
        assertTrue(cache.putIfAbsent(42L, foo));
        assertEquals(1, cache.size());
        assertFalse(cache.putIfAbsent(42L, Map.of("zip", "zot")));
        assertEquals(1, cache.size());

        assertEquals(foo, cache.get(42L));
    }

    private static MapsMemoizer<Long, String> mapsMemoizer() {
        return create(null, HashKind.K128, null);
    }

    private static LeafHasher<HashKind.K128> collidingLeafHasher() {
        com.github.kjetilv.uplift.hash.Hash<HashKind.K128> collider = HashKind.K128.random();
        return _ -> collider;
    }

    private static Map<String, Object> map(IntStream intStream) {
        return intStream
            .mapToObj(i -> entry(String.valueOf(i), i))
            .collect(
                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map<String, Object> build42(Map<String, ? extends Serializable> zot1Zot2) {
        return Map.of(
            "fooTop", "bar",
            "zotCopy", zot1Zot2
        );
    }

    private static Map<String, ? extends Number> hh0hh1() {
        return Map.of(
            "hh0", 1,
            "hh1", new BigDecimal("5.25")
        );
    }

    private static Map<String, ? extends Serializable> zot1Zot2() {
        return Map.of(
            "zot2", true,
            "zot1", 5
        );
    }

    @SuppressWarnings("SameParameterValue")
    private static CaKe getKey(Map<CaKe, ?> map, String key) {
        return map.keySet()
            .stream()
            .filter(k -> k.key().equals(key))
            .findFirst()
            .orElseThrow();
    }

    @SuppressWarnings({"unchecked", "SameParameterValue"})
    private static <K> Object getDeep(Map<K, ?> stringMap44, K one, K two) {
        return ((Map<K, ?>) stringMap44.get(one)).get(two);
    }
}