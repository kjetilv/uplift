package com.github.kjetilv.uplift.json.ffm;

import com.github.kjetilv.flopp.kernel.LineSegments;
import com.github.kjetilv.uplift.json.events.LineSegmentJsonReader;
import com.github.kjetilv.uplift.json.samplegen.User;
import com.github.kjetilv.uplift.json.samplegen.Users;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class LineSegmentJsonReaderTest {

    @Test
    void testMemorySegmentJsonReader() {
        UUID uuid = UUID.randomUUID();
        String json =
            //language=json
            """
                {
                  "name": "Kjetil",
                  "birthYear": 1973,
                  "roadWarrior": true,
                  "aliases": ["MrX", "Foo"],
                  "misc": [50, 60, 70],
                  "balance": "123.23",
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
                          "foo": "bar",
                          "zip": true,
                          "zot": 42
                        }
                      }
                    ]
                  },
                  "maxAge": 127
                }
                """.formatted(uuid);

        LineSegmentJsonReader<User> reader = new LineSegmentJsonReader<>(Users.INSTANCE.callbacks());
        User user = reader.read(LineSegments.of(json, StandardCharsets.UTF_8));
        System.out.println(user);

        User rwRead = Users.INSTANCE.lineSegmentReader().read(LineSegments.of(json));
        assertThat(rwRead.name()).isEqualTo("Kjetil");

        String written = Users.INSTANCE.stringWriter().write(user);
//        User read = reader.read(LineSegments.of(written));
//        assertThat(read).isEqualTo(user);
        User expected = new User(
            "Kjetil",
            1973,
            null,
            new User.Address(
                "\"None\" Street",
                1729,
                User.Address.Modifier.B,
                List.of(User.Address.Modifier.C),
                1450,
                List.of(
                    new User.Address.Resident(
                        "foo",
                        true,
                        null,
                        new LinkedHashMap<>() {{
                            put("zip", "zot");
                        }}
                    ),
                    new User.Address.Resident(
                        "bar",
                        false,
                        uuid,
                        new LinkedHashMap<>() {

                            {
                                put("foo", "bar");
                                put("zip", true);
                                put("zot", 42);
                            }
                        }
                    )
                )
            ),
            true,
            (byte) 127,
            List.of("MrX", "Foo"),
            List.of(50, 60, 70),
            null,
            new BigDecimal("123.23")
        );
        assertThat(rwRead).isEqualTo(expected);
//        assertThat(read.name()).isEqualTo("Kjetil");
//        assertThat(user).isEqualTo(
//            expected
//        );
    }
}
