package com.github.kjetilv.uplift.json.ffm;

import com.github.kjetilv.flopp.kernel.LineSegments;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.events.LineSegmentJsonReader;
import com.github.kjetilv.uplift.json.samplegen.User;
import com.github.kjetilv.uplift.json.samplegen.Users;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

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
                  "birthTime": 100,
                  "maxAge": 127
                }
                """.formatted(uuid);

        Function<Consumer<User>, Callbacks> callbacks = Users.INSTANCE.callbacks();
        LineSegmentJsonReader<User> reader = new LineSegmentJsonReader<>(callbacks);

        User user = reader.read(LineSegments.of(json, StandardCharsets.UTF_8));
        System.out.println(user);

        String written = Users.INSTANCE.stringWriter().write(user);
//        assertThat(reader.read(LineSegments.of(written))).isEqualTo(user);
    }
}
