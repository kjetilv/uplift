package com.github.kjetilv.uplift.edamame.test;

import module java.base;
import com.github.kjetilv.uplift.edamame.Canonicalizer;
import com.github.kjetilv.uplift.edamame.Canonicalizers;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.edamame.MapsMemoizers;
import com.github.kjetilv.uplift.edamame.PojoBytes;
import com.github.kjetilv.uplift.edamame.impl.RecursiveTreeHasher;
import com.github.kjetilv.uplift.edamame.impl.TreeHasher;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind.K128;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static org.assertj.core.api.Assertions.assertThat;

/// Hash invariants: equal values must hash equal, different values must hash different.
///
/// Disabled tests document known defects, by id from the uplift-edamame review. Remove `@Disabled` when fixed.
class HashingTest {

    @Test
    void mapAndListHashDiffer() {
        assertThat(hash(Map.of("", "x"))).isNotEqualTo(hash(List.of("x")));
    }

    @Test
    void emptyMapEmptyListAndNullHashDiffer() {
        assertThat(Stream.of(hash(Map.of()), hash(List.of()), hash(null)).distinct().count()).isEqualTo(3L);
    }

    @Test
    void noCollisionsCanonicalizerKeepsMapAndListApart() {
        var canonicalizer = noCollisionsCanonicalizer();
        canonicalizer.canonical(TREE_HASHER.tree(Map.of("k", Map.of("", "x"))));

        var second = canonicalizer.canonical(TREE_HASHER.tree(Map.of("k", List.of("x")))).value();

        assertThat(second).isEqualTo(Map.of("k", List.of("x")));
    }

    @Disabled("B2: MonthDay is hashed as month * 12 + day")
    @Test
    void monthDayHashesDiffer() {
        assertDifferentHashes(MonthDay.of(1, 13), MonthDay.of(2, 1));
    }

    @Disabled("B2: LocalDateTime nanoseconds are not hashed")
    @Test
    void localDateTimeNanosAreHashed() {
        assertDifferentHashes(
            LocalDateTime.of(2024, 1, 1, 0, 0),
            LocalDateTime.of(2024, 1, 1, 0, 0, 0, 1)
        );
    }

    @Disabled("B2: OffsetTime nanoseconds are not hashed")
    @Test
    void offsetTimeNanosAreHashed() {
        assertDifferentHashes(
            OffsetTime.of(12, 0, 0, 0, ZoneOffset.UTC),
            OffsetTime.of(12, 0, 0, 1, ZoneOffset.UTC)
        );
    }

    @Disabled("B2: ZonedDateTime zone is not hashed")
    @Test
    void zonedDateTimeZoneIsHashed() {
        assertDifferentHashes(
            ZonedDateTime.parse("2024-01-01T12:00Z"),
            ZonedDateTime.parse("2024-01-01T13:00+01:00")
        );
    }

    @Disabled("B2: OffsetDateTime offset is not hashed")
    @Test
    void offsetDateTimeOffsetIsHashed() {
        assertDifferentHashes(
            OffsetDateTime.parse("2024-01-01T12:00Z"),
            OffsetDateTime.parse("2024-01-01T13:00+01:00")
        );
    }

    @Disabled("B2: ChronoLocalDate chronology is not hashed")
    @Test
    void chronologyIsHashed() {
        var isoDate = LocalDate.of(2024, 1, 1);
        assertDifferentHashes(isoDate, JapaneseDate.from(isoDate));
    }

    @Disabled("B2: Era is hashed by number only")
    @Test
    void eraTypeIsHashed() {
        assertDifferentHashes(IsoEra.CE, JapaneseEra.SHOWA);
    }

    @Disabled("B2: LocalDateTime nanoseconds are not hashed")
    @Test
    void noCollisionsCanonicalizerKeepsNanos() {
        var canonicalizer = noCollisionsCanonicalizer();
        canonicalizer.canonical(TREE_HASHER.tree(Map.of("t", LocalDateTime.of(2024, 1, 1, 0, 0, 0, 1))));

        var second = canonicalizer.canonical(TREE_HASHER.tree(Map.of("t", LocalDateTime.of(2024, 1, 1, 0, 0))))
            .value();

        assertThat(second).isEqualTo(Map.of("t", LocalDateTime.of(2024, 1, 1, 0, 0)));
    }

    @Disabled("B3: POJO hash uses the hash builder class, not the leaf class")
    @Test
    void pojoClassIsHashed() {
        assertDifferentHashes(new Left("x"), new Right("x"));
    }

    @Disabled("B4: map hash depends on key iteration order")
    @Test
    void keyOrderDoesNotAffectHash() {
        var random = new Random(42);
        IntStream.range(0, 200).forEach(_ -> {
            var keys = new ArrayList<>(FIELDS.subList(0, 5 + random.nextInt(FIELDS.size() - 5)));
            var ordered = map(keys);
            Collections.shuffle(keys, random);
            var shuffled = map(keys);

            assertThat(hash(shuffled))
                .as("hash of %s vs %s", ordered.keySet(), shuffled.keySet())
                .isEqualTo(hash(ordered));
        });
    }

    @Disabled("B4: map hash depends on key iteration order")
    @Test
    void keyOrderDoesNotAffectSharing() {
        var memoizer = MapsMemoizers.<Long, K128>create(K128);
        var first = new LinkedHashMap<String, Object>();
        first.put("Aa", 1);
        first.put("BB", 2);
        var second = new LinkedHashMap<String, Object>();
        second.put("BB", 2);
        second.put("Aa", 1);

        memoizer.put(1L, first);
        memoizer.put(2L, second);

        assertThat(memoizer.get(2L)).isSameAs(memoizer.get(1L));
    }

    @Disabled("B10: doubles are hashed by raw bits, NaN values that are equal hash different")
    @Test
    void equalNaNsHashEqual() {
        var nan = Double.longBitsToDouble(0x7ff8000000000001L);
        assertThat(Double.valueOf(nan)).isEqualTo(Double.valueOf(Double.NaN));
        assertThat(LEAF_HASHER.hash(nan)).isEqualTo(LEAF_HASHER.hash(Double.NaN));
    }

    private static final LeafHasher<K128> LEAF_HASHER = LeafHasher.create(K128, PojoBytes.TOSTRING);

    private static final TreeHasher<String, K128> TREE_HASHER = new RecursiveTreeHasher<>(
        () -> HashBuilder.forKind(K128),
        Object::toString,
        LEAF_HASHER,
        K128
    );

    private static final List<String> FIELDS = List.of(
        "id", "name", "email", "created", "updated", "status", "type", "owner",
        "tags", "version", "parent", "children", "title", "description", "url", "count"
    );

    private static Object hash(Object value) {
        return TREE_HASHER.tree(value).hash();
    }

    private static void assertDifferentHashes(Object one, Object other) {
        assertThat(one).isNotEqualTo(other);
        assertThat(LEAF_HASHER.hash(one)).isNotEqualTo(LEAF_HASHER.hash(other));
    }

    private static Canonicalizer<String, K128> noCollisionsCanonicalizer() {
        return Canonicalizers.canonicalizer(true);
    }

    private static Map<String, Object> map(List<String> keys) {
        var map = new LinkedHashMap<String, Object>();
        keys.forEach(key -> map.put(key, key + "-value"));
        return map;
    }

    private record Left(String value) {

        @Override
        public String toString() {
            return value;
        }
    }

    private record Right(String value) {

        @Override
        public String toString() {
            return value;
        }
    }
}
