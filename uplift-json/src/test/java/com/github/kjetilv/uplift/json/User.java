package com.github.kjetilv.uplift.json;

public record User(
    String name,
    Integer birthYear,
    Address address,
    boolean roadWarrior) {

}

