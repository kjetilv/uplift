package com.github.kjetilv.uplift.edamame.impl;

import module java.base;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.edamame.PojoBytes;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.util.Bytes;

import static com.github.kjetilv.uplift.edamame.impl.Tag.*;

/// This default [leaf hasher][LeafHasher] supports typical Java classes, often
/// found in trees.
///
/// @see Tag
public record DefaultLeafHasher<H extends HashKind<H>>(Supplier<HashBuilder<Bytes, H>> newBuilder, PojoBytes pojoBytes)
    implements LeafHasher<H> {

    public DefaultLeafHasher(Supplier<HashBuilder<Bytes, H>> newBuilder, PojoBytes pojoBytes) {
        this.newBuilder = Objects.requireNonNull(newBuilder, "newBuilder");
        this.pojoBytes = Objects.requireNonNull(pojoBytes, "pojoBytes");
    }

    @Override
    public Hash<H> hash(Object leaf) {
        return hashTo(newBuilder.get(), leaf).build();
    }

    private HashBuilder<Bytes, H> hashTo(HashBuilder<Bytes, H> hb, Object leaf) {
        return switch (leaf) {
            case String s -> hashString(STRING.tag(hb), s);
            case Boolean b -> hashString(BOOL.tag(hb), Boolean.toString(b));
            case BigDecimal b -> hashBigDecimal(BIG_DECIMAL.tag(hb), b);
            case BigInteger b -> hashBigInteger(BIG_INTEGER.tag(hb), b);
            case UUID u -> hashUuid(UUID.tag(hb), u);
            case Number n -> hashNumber(NUMBER.tag(hb), n);
            case TemporalAccessor temporal -> hashTemporal(hb, temporal);
            default -> hashLeaf(OBJECT.tag(hb), leaf, pojoBytes);
        };
    }

    private static <K extends HashKind<K>> HashBuilder<Bytes, K> hashNumber(
        HashBuilder<Bytes, K> hb,
        Number n
    ) {
        return switch (n) {
            case Long l -> LONG.tag(hb).hash(Bytes.longBytes(l));
            case Integer i -> INT.tag(hb).hash(Bytes.intBytes(i));
            // Non-raw bits: all NaN values are equal, as in Double.equals and Float.equals
            case Double d -> DOUBLE.tag(hb).hash(Bytes.longBytes(Double.doubleToLongBits(d)));
            case Float f -> FLOAT.tag(hb).hash(Bytes.intBytes(Float.floatToIntBits(f)));
            case Short s -> SHORT.tag(hb).hash(Bytes.intBytes(s));
            case Byte b -> BYTE.tag(hb).hash(Bytes.from(b));
            default -> hashString(hashText(OTHER_NUMERIC.tag(hb), n.getClass().getName()), n.toString());
        };
    }

    /// Hashes all fields compared by the type's `equals` method.
    private static <K extends HashKind<K>> HashBuilder<Bytes, K> hashTemporal(
        HashBuilder<Bytes, K> hb,
        TemporalAccessor t
    ) {
        return switch (t) {
            case Instant i -> hashInstant(INSTANT.tag(hb), i);
            case ChronoLocalDate d -> hashLongs(
                hashText(LOCAL_DATE.tag(hb), d.getChronology().getId()),
                d.toEpochDay()
            );
            case ChronoLocalDateTime<?> l -> hashLongs(
                hashText(LOCAL_DATE_TIME.tag(hb), l.getChronology().getId()),
                l.toLocalDate().toEpochDay(),
                l.toLocalTime().toNanoOfDay()
            );
            case ChronoZonedDateTime<?> z -> hashLongs(
                hashText(hashText(ZONED_DATETIME.tag(hb), z.getChronology().getId()), z.getZone().getId()),
                z.toEpochSecond(),
                z.toLocalTime().getNano(),
                z.getOffset().getTotalSeconds()
            );
            case OffsetTime o -> hashLongs(
                OFFSET_TIME.tag(hb),
                o.toLocalTime().toNanoOfDay(),
                o.getOffset().getTotalSeconds()
            );
            case OffsetDateTime o -> hashLongs(
                OFFSET_DATETIME.tag(hb),
                o.toEpochSecond(),
                o.getNano(),
                o.getOffset().getTotalSeconds()
            );
            case Year y -> hashLongs(YEAR.tag(hb), y.getValue());
            case YearMonth y -> hashLongs(YEAR_MONTH.tag(hb), y.getYear(), y.getMonthValue());
            case Month m -> hashLongs(MONTH.tag(hb), m.getValue());
            case MonthDay m -> hashLongs(MONTH_DAY.tag(hb), m.getMonthValue(), m.getDayOfMonth());
            case DayOfWeek d -> hashLongs(DAY_OF_WEEK.tag(hb), d.getValue());
            case Era e -> hashLongs(hashText(ERA.tag(hb), e.getClass().getName()), e.getValue());
            case ZoneOffset z -> hashLongs(ZONE_OFFSET.tag(hb), z.getTotalSeconds());
            case TemporalAccessor ta -> hashText(hashText(OTHER_TEMPORAL.tag(hb), ta.getClass().getName()), ta.toString());
        };
    }

    private static <K extends HashKind<K>> HashBuilder<Bytes, K> hashLongs(
        HashBuilder<Bytes, K> hb,
        long... values
    ) {
        for (var value : values) {
            hb.hash(Bytes.longBytes(value));
        }
        return hb;
    }

    /// Hashes the length, then the string. The length keeps consecutive strings apart.
    private static <K extends HashKind<K>> HashBuilder<Bytes, K> hashText(
        HashBuilder<Bytes, K> hb,
        String string
    ) {
        var bytes = Bytes.from(string);
        return hb.hash(Bytes.intBytes(bytes.length())).hash(bytes);
    }

    /// Hashes the leaf's class name, then the [PojoBytes] of the leaf. The class name keeps leaves
    /// of different classes apart when their pojo bytes are equal.
    private static <K extends HashKind<K>> HashBuilder<Bytes, K> hashLeaf(
        HashBuilder<Bytes, K> hb,
        Object leaf,
        PojoBytes anyHash
    ) {
        return hashText(hb, leaf.getClass().getName())
            .hash(anyHash.bytes(leaf));
    }

    private static <K extends HashKind<K>> HashBuilder<Bytes, K> hashString(
        HashBuilder<Bytes, K> hb,
        String string
    ) {
        return hb.hash(Bytes.from(string));
    }

    private static <K extends HashKind<K>> HashBuilder<Bytes, K> hashInstant(
        HashBuilder<Bytes, K> hb,
        Instant instant
    ) {
        hb.map(Bytes::longBytes)
            .hash(instant.getEpochSecond())
            .hash((long) instant.getNano());
        return hb;
    }

    private static <K extends HashKind<K>> HashBuilder<Bytes, K> hashUuid(
        HashBuilder<Bytes, K> hb,
        UUID uuid
    ) {
        hb.map(Bytes::longBytes)
            .hash(uuid.getMostSignificantBits())
            .hash(uuid.getLeastSignificantBits());
        return hb;
    }

    private static <K extends HashKind<K>> HashBuilder<Bytes, K> hashBigDecimal(
        HashBuilder<Bytes, K> hb,
        BigDecimal bigDecimal
    ) {
        return hb.hash(Bytes.from(bigDecimal))
            .hash(Bytes.intBytes(bigDecimal.scale()));
    }

    private static <K extends HashKind<K>> HashBuilder<Bytes, K> hashBigInteger(
        HashBuilder<Bytes, K> hashBuilder,
        BigInteger bigInteger
    ) {
        return hashBuilder.hash(Bytes.from(bigInteger));
    }
}
