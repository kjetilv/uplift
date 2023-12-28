package com.github.kjetilv.uplift.uuid;

import java.util.UUID;

import com.github.kjetilv.uplift.uuid.Uuid;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UuidTest {

    @Test
    void should_be_22_chars() {
        Uuid random = Uuid.random();
        assertEquals(22, random.digest().length());
        assertEquals(36, random.uuid().toString().length());
    }

    @Test
    void should_convert_back_and_forth() {
        for (int i = 0; i < 100_000; i++) {

            UUID uuid = UUID.randomUUID();

            Uuid uuidFromUuid = Uuid.from(uuid);
            Uuid uuidFromString = Uuid.from(uuidFromUuid.digest());

            assertEquals(uuidFromUuid, uuidFromString);

            assertEquals(uuidFromUuid.digest(), uuidFromString.digest());
            assertEquals(uuidFromUuid.uuid(), uuidFromString.uuid());
        }
    }
}
