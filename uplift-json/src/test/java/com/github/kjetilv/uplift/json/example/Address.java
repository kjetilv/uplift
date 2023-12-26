package com.github.kjetilv.uplift.json.example;

import com.github.kjetilv.uplift.json.anno.JsRec;

import java.util.List;

@JsRec
public record Address(
    String streetName,
    Integer houseNumber,
    Modifier modifier,
    List<Modifier> adjacents,
    Integer code,
    List<Resident> residents
) {

    public enum Modifier {
        A, B, C
    }
}
