package com.github.kjetilv.uplift.json.samplegen;

import com.github.kjetilv.uplift.json.anno.JsonRecord;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@JsonRecord(factoryClass = "Users")
public record User(
    String name,
    Integer birthYear,
    Instant birthTime,
    Address address,
    boolean roadWarrior,
    byte maxAge,
    List<String> aliases,
    List<Integer> misc,
    BigDecimal balance) {

}

