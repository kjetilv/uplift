package com.github.kjetilv.uplift.json.samplegen;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonRecordTest {

    @Test
    void parseUser() {
        String json = """
            {
              "name": "Kjetil",
              "address": {
                "streetName": "None Street",
                "houseNumber": 1729,
                "modifier": "B",
                "adjacents": [ "C" ],
                "code": 1450,
                "residents": [
                  {
                    "name": "foo",
                    "permanent": true
                  },
                  {
                    "name": "bar",
                    "permanent": false
                  }
                ]
              },
              "roadWarrior": true,
              "birthYear": 1973,
              "aliases": ["MrX", "Foo"],
              "misc": [50, 60, 70],
              "maxAge": 127,
              "balance": "123.23"
            }
            """;
        assertThat(com.github.kjetilv.uplift.json.samplegen.Users.INSTANCE.read(json)).isEqualTo(
            new User(
                "Kjetil",
                1973,
                new Address(
                    "None Street",
                    1729,
                    Address.Modifier.B,
                    List.of(Address.Modifier.C),
                    1450,
                    List.of(
                        new Resident(
                            "foo", true
                        ),
                        new Resident(
                            "bar", false
                        )
                    )
                ),
                true,
                (byte) 127,
                List.of("MrX", "Foo"),
                List.of(
                    50,
                    60,
                    70
                ),
                new BigDecimal("123.23")
            ));
        String addressJson = """
            { "address":
              {
                "streetName": "None Street",
                "houseNumber": 1729,
                "modifier": "B",
                "adjacents": [ "C" ],
                "code": 1450
              }
            }
            """;
        assertThat(com.github.kjetilv.uplift.json.samplegen.Users.INSTANCE.read(addressJson).address())
            .isEqualTo(
                new Address(
                    "None Street",
                    1729,
                    Address.Modifier.B,
                    List.of(Address.Modifier.C),
                    1450,
                    null
                )
            );
    }
}
