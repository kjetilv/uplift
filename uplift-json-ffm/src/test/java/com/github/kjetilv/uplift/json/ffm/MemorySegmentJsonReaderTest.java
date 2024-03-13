package com.github.kjetilv.uplift.json.ffm;

import com.github.kjetilv.flopp.kernel.LineSegments;
import com.github.kjetilv.uplift.json.samplegen.User;
import com.github.kjetilv.uplift.json.samplegen.UserCallbacks;
import com.github.kjetilv.uplift.json.samplegen.Users;
import org.junit.jupiter.api.Test;

import java.util.UUID;

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

        MemorySegmentJsonReader<User, UserCallbacks> reader =
            new MemorySegmentJsonReader<>(Users.callbacks());

        User user = reader.read(LineSegments.of(json));
        System.out.println(user);
    }
}
