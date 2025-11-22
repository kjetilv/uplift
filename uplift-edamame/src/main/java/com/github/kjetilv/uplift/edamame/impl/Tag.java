package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;

enum Tag {
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
