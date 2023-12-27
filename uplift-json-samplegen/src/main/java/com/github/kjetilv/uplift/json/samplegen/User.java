package com.github.kjetilv.uplift.json.samplegen;

import com.github.kjetilv.uplift.json.anno.JsonRecord;

import java.math.BigDecimal;
import java.util.List;

@JsonRecord(factoryClass = "Users", root = true)
public record User(
    String name,
    Integer birthYear,
    Address address,
    boolean roadWarrior,
    byte maxAge,
    List<String> aliases,
    List<Integer> misc,
    BigDecimal balance) {

}

