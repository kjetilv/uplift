package com.github.kjetilv.uplift.json.samplegen;

import com.github.kjetilv.uplift.json.events.JsonReader;
import com.github.kjetilv.uplift.json.events.JsonWriter;
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
        //language=json
        String json =
            """
                {
                  "name": "Kjetil",
                  "address": {
                    "streetName": "\\"None\\" Street",
                    "houseNumber": 1729,
                    "modifier": "B",
                    "adjacents": [ "C" ],
                    "code": 1450,
                    "unrecognized": "gurba",
                    "unrecognized2": {
                      "foo": "gurba"
                    },
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
        User readUser = STRING_READER.read(json);
        User expectedUser = new User(
            "Kjetil",
            1973,
            Instant.ofEpochMilli(100L),
            new User.Address(
                "\"None\" Street",
                1729,
                User.Address.Modifier.B,
                List.of(User.Address.Modifier.C),
                1450,
                List.of(
                    new User.Address.Resident(
                        "foo", true,
                        null,
                        Map.of("zip", "zot")
                    ),
                    new User.Address.Resident(
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
        User genUser = Users.INSTANCE.stringReader().read(json);
        assertThat(genUser).isEqualTo(expectedUser);

        assertThat(readUser.toString()).isEqualTo(expectedUser.toString());
        String addressJson =
            //language=json
            """
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
        assertThat(STRING_READER
            .read(addressJson)
            .address())
            .isEqualTo(
                new User.Address(
                    "None Street",
                    1729,
                    User.Address.Modifier.B,
                    List.of(User.Address.Modifier.C),
                    1450,
                    null
                )
            );

        StringBuilder sb = new StringBuilder();
//        Callbacks callbacks = JsonWriter.writer(user, new StringSink(sb));
//        callbacks.objectStarted()
//            .field("name").string(user.name())
//            .field("birthYear").number(user.birthYear())
//            .field("address")
//            .objectStarted().field("streetName").string(user.address().streetName()).objectEnded()
//            .field("aliases").arrayStarted()

        WRITER.write(readUser, sb);

        User read2 = STRING_READER.read(sb.toString());

        assertThat(readUser).isEqualTo(read2);
    }

    public static final JsonReader<String, User> STRING_READER =
        Users.INSTANCE.stringReader();

    public static final JsonWriter<String, User, StringBuilder> WRITER =
        Users.INSTANCE.stringWriter();
}
