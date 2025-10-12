package com.github.kjetilv.uplift.uuid;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UuidTest {

    @Test
    void should_be_22_chars() {
        var random = Uuid.random();
        assertEquals(22, random.digest().length());
        assertEquals(36, random.uuid().toString().length());
    }

    @Test
    void should_convert_back_and_forth() {
        for (var i = 0; i < 100_000; i++) {

            var uuid = UUID.randomUUID();

            var uuidFromUuid = Uuid.from(uuid);
            var uuidFromString = Uuid.from(uuidFromUuid.digest());

            assertEquals(uuidFromUuid, uuidFromString);

            assertEquals(uuidFromUuid.digest(), uuidFromString.digest());
            assertEquals(uuidFromUuid.uuid(), uuidFromString.uuid());
        }
    }
}
