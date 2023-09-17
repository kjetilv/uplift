package com.github.kjetilv.uplift.json;

import java.util.List;

public record Address(
    String streetName,
    Integer houseNumber,
    Modifier modifier,
    Integer code,
    List<Resident> residents
) {

    public enum Modifier {
        A, B, C
    }
}
