package com.github.kjetilv.uplift.edamame.impl;

import module java.base;
import module uplift.edamame;
import module uplift.hash;

/// This default [leaf hasher][LeafHasher] supports typical Java classes, often
/// found in trees.
///
/// @see T
public record DefaultLeafHasher<H extends HashKind<H>>(Supplier<HashBuilder<byte[], H>> newBuilder, PojoBytes pojoBytes)
    implements LeafHasher<H> {

    public DefaultLeafHasher(Supplier<HashBuilder<byte[], H>> newBuilder, PojoBytes pojoBytes) {
        this.newBuilder = Objects.requireNonNull(newBuilder, "newBuilder");
        this.pojoBytes = Objects.requireNonNull(pojoBytes, "pojoBytes");
    }

    @Override
    public Hash<H> hash(Object leaf) {
        return hashTo(newBuilder.get(), leaf).build();
    }

    private HashBuilder<byte[], H> hashTo(HashBuilder<byte[], H> hb, Object leaf) {
        return switch (leaf) {
            case String s -> hashString(T.STRING.tag(hb), s);
            case Boolean b -> hashString(T.BOOL.tag(hb), Boolean.toString(b));
            case BigDecimal b -> hashBigDecimal(T.BIG_DECIMAL.tag(hb), b);
            case BigInteger b -> hashBigInteger(T.BIG_INTEGER.tag(hb), b);
            case Number n -> hashNumber(T.NUMBER.tag(hb), n);
            case TemporalAccessor temporal -> hashTemporal(hb, temporal);
            case UUID u -> hashUuid(T.UUID.tag(hb), u);
            default -> hashLeaf(T.OBJECT.tag(hb), leaf, pojoBytes);
        };
    }

    private static <H extends HashKind<H>> HashBuilder<byte[], H> hashNumber(
        HashBuilder<byte[], H> hb,
        Number n
    ) {
        return switch (n) {
            case Long l -> T.LONG.tag(hb).hash(Hashes.longBytes(l));
            case Integer i -> T.INT.tag(hb).hash(Hashes.intBytes(i));
            case Double d -> T.DOUBLE.tag(hb).hash(Hashes.longBytes(Double.doubleToRawLongBits(d)));
            case Float f -> T.FLOAT.tag(hb).hash(Hashes.intBytes(Float.floatToRawIntBits(f)));
            case Short s -> T.SHORT.tag(hb).hash(Hashes.intBytes(s));
            case Byte b -> T.BYTE.tag(hb).hash(new byte[] {(byte) (int) b});
            default -> hashString(T.OTHER_NUMERIC.tag(hb), n.toString());
        };
    }

    private static <H extends HashKind<H>> HashBuilder<byte[], H> hashTemporal(
        HashBuilder<byte[], H> hb,
        TemporalAccessor t
    ) {
        return switch (t) {
            case Instant i -> hashInstant(T.INSTANT.tag(hb), i);
            case ChronoLocalDate l -> hashNumber(T.LOCAL_DATE.tag(hb), l.toEpochDay());
            case ChronoLocalDateTime<?> l -> hashNumber(T.LOCAL_DATE_TIME.tag(hb), l.toEpochSecond(ZoneOffset.UTC));
            case ChronoZonedDateTime<?> z -> hashNumber(T.ZONED_DATETIME.tag(hb), z.toEpochSecond());
            case OffsetTime o -> hashNumber(T.OFFSET_TIME.tag(hb), o.toEpochSecond(LocalDate.EPOCH));
            case OffsetDateTime o -> hashNumber(T.OFFSET_DATETIME.tag(hb), o.toEpochSecond());
            case Year y -> hashNumber(T.YEAR.tag(hb), y.getValue());
            case YearMonth y -> hashNumber(T.YEAR_MONTH.tag(hb), y.getYear() * 12 + y.getMonthValue());
            case Month m -> hashNumber(T.MONTH.tag(hb), m.getValue());
            case MonthDay m -> hashNumber(T.MONTH_DAY.tag(hb), m.getMonthValue() * 12 + m.getDayOfMonth());
            case DayOfWeek d -> hashNumber(T.DAY_OF_WEEK.tag(hb), d.getValue());
            case Era e -> hashNumber(T.ERA.tag(hb), e.getValue());
            case TemporalAccessor ta -> hashString(T.OTHER_TEMPORAL.tag(hb), ta.toString());
        };
    }

    private static <H extends HashKind<H>> HashBuilder<byte[], H> hashLeaf(
        HashBuilder<byte[], H> hb,
        Object leaf,
        PojoBytes anyHash
    ) {
        return hb
            .hash(hb.getClass().getName().getBytes())
            .hash(anyHash.bytes(leaf));
    }

    private static <H extends HashKind<H>> HashBuilder<byte[], H> hashString(
        HashBuilder<byte[], H> hb,
        String string
    ) {
        return hb.hash(string.getBytes());
    }

    private static <H extends HashKind<H>> HashBuilder<byte[], H> hashInstant(
        HashBuilder<byte[], H> hb,
        Instant instant
    ) {
        hb.<Long>map(Hashes::longBytes)
            .hash(instant.getEpochSecond())
            .hash((long) instant.getNano());
        return hb;
    }

    private static <H extends HashKind<H>> HashBuilder<byte[], H> hashUuid(
        HashBuilder<byte[], H> hb,
        UUID uuid
    ) {
        hb.<Long>map(Hashes::longBytes)
            .hash(uuid.getMostSignificantBits())
            .hash(uuid.getLeastSignificantBits());
        return hb;
    }

    private static <H extends HashKind<H>> HashBuilder<byte[], H> hashBigDecimal(
        HashBuilder<byte[], H> hb,
        BigDecimal bigDecimal
    ) {
        return hb.hash(bigDecimal.unscaledValue().toByteArray())
            .hash(Hashes.intBytes(bigDecimal.scale()));
    }

    private static <H extends HashKind<H>> HashBuilder<byte[], H> hashBigInteger(
        HashBuilder<byte[], H> hashBuilder,
        BigInteger bigInteger
    ) {
        return hashBuilder.hash(bigInteger.toByteArray());
    }

    private enum T {
        OBJECT,
        STRING,
        BOOL,
        NUMBER,
        DOUBLE,
        FLOAT,
        LONG,
        INT,
        SHORT,
        BYTE,
        BIG_DECIMAL,
        BIG_INTEGER,
        OTHER_NUMERIC,
        LOCAL_DATE,
        LOCAL_DATE_TIME,
        ZONED_DATETIME,
        OFFSET_TIME,
        OFFSET_DATETIME,
        YEAR,
        YEAR_MONTH,
        MONTH,
        MONTH_DAY,
        DAY_OF_WEEK,
        ERA,
        INSTANT,
        OTHER_TEMPORAL,
        UUID;

        private final byte[] bytes = {(byte) ordinal()};

        <H extends HashKind<H>> HashBuilder<byte[], H> tag(HashBuilder<byte[], H> hb) {
            return hb.hash(bytes);
        }
    }
}
