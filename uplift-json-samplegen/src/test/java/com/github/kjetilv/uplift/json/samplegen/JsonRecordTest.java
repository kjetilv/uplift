package com.github.kjetilv.uplift.json.samplegen;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonRecordTest {

    @Test
    void parseUser() {
        UUID uuid = UUID.randomUUID();
        String json = """
            {
              "name": "Kjetil",
              "address": {
                "streetName": "\\"None\\" Street",
                "houseNumber": 1729,
                "modifier": "B",
                "adjacents": [ "C" ],
                "code": 1450,
                "residents": [
                  {
                    "name": "foo",
                    "permanent": true,
                    "properties": {
                      "zip": "zot"
                    }
                  },
                  {
                    "name": "bar",
                    "permanent": false,
                    "uuid": "%s",
                    "properties": {
                      "foo": "bar"
                    }
                  }
                ]
              },
              "roadWarrior": true,
              "birthYear": 1973,
              "birthTime": 100,
              "aliases": ["MrX", "Foo"],
              "misc": [50, 60, 70],
              "maxAge": 127,
              "balance": "123.23"
            }
            """.formatted(uuid);
        User readUser = Users.INSTANCE.read(json);
        User expectedUser = new User(
            "Kjetil",
            1973,
            Instant.ofEpochMilli(100L),
            new Address(
                "\"None\" Street",
                1729,
                Address.Modifier.B,
                List.of(Address.Modifier.C),
                1450,
                List.of(
                    new Resident(
                        "foo", true,
                        null,
                        Map.of("zip", "zot")
                    ),
                    new Resident(
                        "bar", false, uuid, Map.of("foo", "bar")
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
        );
        assertThat(readUser.toString()).isEqualTo(expectedUser.toString());
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

        User user = readUser;

        StringBuilder sb = new StringBuilder();
//        Callbacks callbacks = JsonWriter.writer(user, new StringSink(sb));
//        callbacks.objectStarted()
//            .field("name").string(user.name())
//            .field("birthYear").number(user.birthYear())
//            .field("address")
//            .objectStarted().field("streetName").string(user.address().streetName()).objectEnded()
//            .field("aliases").arrayStarted()

        Users.INSTANCE.write(user, sb);

        System.out.println(sb);

        User read2 = Users.INSTANCE.read(sb.toString());

        assertThat(user).isEqualTo(read2);
    }
}
