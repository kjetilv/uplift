package com.github.kjetilv.uplift.json.samplegen;

import com.github.kjetilv.uplift.json.JsonReader;
import com.github.kjetilv.uplift.json.JsonWriter;
import com.github.kjetilv.uplift.json.mame.CachingJsonSessions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;

public class JsonRecordTest {

    @Test
    void parseShortUser() {
        var json =
            //language=json
            """
            {
              "tags": {
                "foos": {
                  "foo1": "bar",
                  "foo2": "zot"
                }
              }
            }
            """;
        var readUser = Users.INSTANCE.stringReader(CachingJsonSessions.create128()).read(json);
        System.out.println(readUser);

//        assertThat(readUser.tags()).containsEntry("good", "evil");
//        assertThat(readUser.tags()).containsEntry("1", 2L);
        assertThat(readUser.tags().get("foos"))
            .asInstanceOf(MAP)
            .containsEntry("foo1", "bar")
            .containsEntry("foo2", "zot");
    }

    @Test
    void parseUser() {
        var uuid = UUID.randomUUID();
        //language=json
        var json =
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
                  "balance": "123.23",
                  "tags": {
                    "good": "evil",
                    "1": 2,
                    "foo": true
                  }
                }
                """.formatted(uuid);
        var readUser = STRING_READER.read(json);
        var expectedUser = new User(
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
            Stream.of(
                    Map.entry("good", "evil"),
                    Map.entry("1", 2L),
                    Map.entry("foo", true)
                )
                .collect(Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue, (o, _) -> o,
                    LinkedHashMap::new
                )),
            new BigDecimal("123.23")
        );
        var genUser = Users.INSTANCE.stringReader().read(json);
        assertThat(genUser).isEqualTo(expectedUser);

        assertThat(readUser.toString()).isEqualTo(expectedUser.toString());
        var addressJson =
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

        var sb = new StringBuilder();
//        Callbacks callbacks = JsonWriter.writer(user, new StringSink(sb));
//        callbacks.objectStarted()
//            .field("name").string(user.name())
//            .field("birthYear").number(user.birthYear())
//            .field("address")
//            .objectStarted().field("streetName").string(user.address().streetName()).objectEnded()
//            .field("aliases").arrayStarted()

        WRITER.write(readUser, sb);

        var read2 = STRING_READER.read(sb.toString());

        assertThat(readUser).isEqualTo(read2);
    }

    public static final JsonReader<String, User> STRING_READER =
        Users.INSTANCE.stringReader();

    public static final JsonWriter<String, User, StringBuilder> WRITER =
        Users.INSTANCE.stringWriter();
}
