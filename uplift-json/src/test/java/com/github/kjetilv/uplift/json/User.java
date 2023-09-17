package com.github.kjetilv.uplift.json;

import java.util.List;

public record User(
    String name,
    Integer birthYear,
    Address address,
    boolean roadWarrior,
    byte maxAge,
    List<Object> misc) {

}

