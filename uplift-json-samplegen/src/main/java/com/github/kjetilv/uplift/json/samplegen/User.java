package com.github.kjetilv.uplift.json.samplegen;

import com.github.kjetilv.uplift.json.anno.JsRec;

import java.util.List;

@JsRec
public record User(
    String name,
    Integer birthYear,
    Address address,
    boolean roadWarrior,
    byte maxAge,
    List<String> aliases,
    List<Integer> misc) {

}

