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
            case Number n -> hashNumber(NUMBER.tag(hb), n);
            case TemporalAccessor temporal -> hashTemporal(hb, temporal);
            case UUID u -> hashUuid(UUID.tag(hb), u);
            default -> hashLeaf(OBJECT.tag(hb), leaf, pojoBytes);
        };
    }

    private static <H extends HashKind<H>> HashBuilder<Bytes, H> hashNumber(
        HashBuilder<Bytes, H> hb,
        Number n
    ) {
        return switch (n) {
            case Long l -> LONG.tag(hb).hash(Bytes.longBytes(l));
            case Integer i -> INT.tag(hb).hash(Bytes.intBytes(i));
            case Double d -> DOUBLE.tag(hb).hash(Bytes.longBytes(Double.doubleToRawLongBits(d)));
            case Float f -> FLOAT.tag(hb).hash(Bytes.intBytes(Float.floatToRawIntBits(f)));
            case Short s -> SHORT.tag(hb).hash(Bytes.intBytes(s));
            case Byte b -> BYTE.tag(hb).hash(Bytes.from(b));
            default -> hashString(OTHER_NUMERIC.tag(hb), n.toString());
        };
    }

    private static <H extends HashKind<H>> HashBuilder<Bytes, H> hashTemporal(
        HashBuilder<Bytes, H> hb,
        TemporalAccessor t
    ) {
        return switch (t) {
            case Instant i -> hashInstant(INSTANT.tag(hb), i);
            case ChronoLocalDate l -> hashNumber(LOCAL_DATE.tag(hb), l.toEpochDay());
            case ChronoLocalDateTime<?> l -> hashNumber(LOCAL_DATE_TIME.tag(hb), l.toEpochSecond(ZoneOffset.UTC));
            case ChronoZonedDateTime<?> z -> hashNumber(ZONED_DATETIME.tag(hb), z.toEpochSecond());
            case OffsetTime o -> hashNumber(OFFSET_TIME.tag(hb), o.toEpochSecond(LocalDate.EPOCH));
            case OffsetDateTime o -> hashNumber(OFFSET_DATETIME.tag(hb), o.toEpochSecond());
            case Year y -> hashNumber(YEAR.tag(hb), y.getValue());
            case YearMonth y -> hashNumber(YEAR_MONTH.tag(hb), y.getYear() * 12 + y.getMonthValue());
            case Month m -> hashNumber(MONTH.tag(hb), m.getValue());
            case MonthDay m -> hashNumber(MONTH_DAY.tag(hb), m.getMonthValue() * 12 + m.getDayOfMonth());
            case DayOfWeek d -> hashNumber(DAY_OF_WEEK.tag(hb), d.getValue());
            case Era e -> hashNumber(ERA.tag(hb), e.getValue());
            case TemporalAccessor ta -> hashString(OTHER_TEMPORAL.tag(hb), ta.toString());
        };
    }

    private static <H extends HashKind<H>> HashBuilder<Bytes, H> hashLeaf(
        HashBuilder<Bytes, H> hb,
        Object leaf,
        PojoBytes anyHash
    ) {
        return hb
            .hash(Bytes.from(hb.getClass().getName()))
            .hash(anyHash.bytes(leaf));
    }

    private static <H extends HashKind<H>> HashBuilder<Bytes, H> hashString(
        HashBuilder<Bytes, H> hb,
        String string
    ) {
        return hb.hash(Bytes.from(string));
    }

    private static <H extends HashKind<H>> HashBuilder<Bytes, H> hashInstant(
        HashBuilder<Bytes, H> hb,
        Instant instant
    ) {
        hb.map(Bytes::longBytes)
            .hash(instant.getEpochSecond())
            .hash((long) instant.getNano());
        return hb;
    }

    private static <H extends HashKind<H>> HashBuilder<Bytes, H> hashUuid(
        HashBuilder<Bytes, H> hb,
        UUID uuid
    ) {
        hb.map(Bytes::longBytes)
            .hash(uuid.getMostSignificantBits())
            .hash(uuid.getLeastSignificantBits());
        return hb;
    }

    private static <H extends HashKind<H>> HashBuilder<Bytes, H> hashBigDecimal(
        HashBuilder<Bytes, H> hb,
        BigDecimal bigDecimal
    ) {
        return hb.hash(Bytes.from(bigDecimal))
            .hash(Bytes.intBytes(bigDecimal.scale()));
    }

    private static <H extends HashKind<H>> HashBuilder<Bytes, H> hashBigInteger(
        HashBuilder<Bytes, H> hashBuilder,
        BigInteger bigInteger
    ) {
        return hashBuilder.hash(Bytes.from(bigInteger));
    }
}
