package com.github.kjetilv.uplift.json;

public record Address(
    String streetName,
    Integer houseNumber,
    Modifier modifier,
    Integer code
) {

    public enum Modifier {
        A, B, C
    }
}
