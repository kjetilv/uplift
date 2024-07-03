package com.github.kjetilv.uplift.json.ffm;

import com.github.kjetilv.flopp.kernel.LineSegments;
import com.github.kjetilv.uplift.json.samplegen.User;
import com.github.kjetilv.uplift.json.samplegen.User_Callbacks;
import com.github.kjetilv.uplift.json.samplegen.Users;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class MemorySegmentJsonReaderTest {

    @Test
    void testMemorySegmentJsonReader() {
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
                      "foo": "bar",
                      "zip": true,
                      "zot": 42,
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

        MemorySegmentJsonReader<User, User_Callbacks> reader =
            new MemorySegmentJsonReader<>(Users.INSTANCE.callbacks());

        User user = reader.read(LineSegments.of(json, StandardCharsets.UTF_8));
        System.out.println(user);

        String written = Users.INSTANCE.stringWriter().write(user);
        assertThat(reader.read(LineSegments.of(written))).isEqualTo(user);
    }
}
