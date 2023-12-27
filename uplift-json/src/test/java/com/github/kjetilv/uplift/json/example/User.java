package com.github.kjetilv.uplift.json.example;

import java.math.BigDecimal;
import java.util.List;

public record User(
    String name,
    Integer birthYear,
    Address address,
    boolean roadWarrior,
    byte maxAge,
    List<String> aliases,
    List<Integer> misc) {

}

