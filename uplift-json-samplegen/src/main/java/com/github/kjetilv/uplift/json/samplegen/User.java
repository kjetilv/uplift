package com.github.kjetilv.uplift.json.samplegen;

import module java.base;
import module uplift.json.anno;

@JsonRecord(factoryClass = "Users")
public record User(
    String name,
    Integer birthYear,
    Instant birthTime,
    Address address,
    boolean roadWarrior,
    byte maxAge,
    @Singular("alias")
    List<String> aliases,
    List<Integer> misc,
    Map<String, Object> tags,
    BigDecimal balance
) {

    public record Address(
        String streetName,
        Integer houseNumber,
        Modifier modifier,
        List<Modifier> adjacents,
        Integer code,
        @Singular("rezzie") List<Resident> residents
    ) {

        public enum Modifier {
            A, B, C, D, E
        }

        public record Resident(String name, boolean permanent, UUID uuid, Map<String, Object> properties) {
        }
    }
}

