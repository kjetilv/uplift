package com.github.kjetilv.uplift.json;

import java.util.List;

public record User(
    String name,
    Integer birthYear,
    Address address,
    boolean roadWarrior,
    List<Object> misc) {

}

