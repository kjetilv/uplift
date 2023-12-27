package com.github.kjetilv.uplift.json.samplegen;

import com.github.kjetilv.uplift.json.anno.JsonRecord;
import com.github.kjetilv.uplift.json.anno.Singular;

import java.util.List;

@JsonRecord
public record Address(
    String streetName,
    Integer houseNumber,
    Modifier modifier,
    List<Modifier> adjacents,
    Integer code,
    @Singular("rezzie") List<Resident> residents
) {

    public enum Modifier {
        A, B, C, D, E
    }
}
