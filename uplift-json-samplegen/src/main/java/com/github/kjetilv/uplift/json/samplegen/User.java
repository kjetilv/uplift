package com.github.kjetilv.uplift.json.samplegen;

import com.github.kjetilv.uplift.json.anno.JsonRecord;
import com.github.kjetilv.uplift.json.anno.Singular;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonRecord(factoryClass = "Users", root = true)
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

