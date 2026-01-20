package com.github.kjetilv.uplift.json.samplegen;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.channels.Channels;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class IOTest {

    @Test
    void test() throws IOException {
        var writer = Users.INSTANCE.channelWriter(64);
        var baos = new ByteArrayOutputStream();
        try (
            var channel = Channels.newChannel(baos)
        ) {
            writer.write(USER, channel);
        }
        var userJson = baos.toString();
        User user;
        try {
            user = Users.INSTANCE.stringReader().read(userJson);
        } catch (Exception e) {
            throw new IllegalStateException(userJson, e);
        }
        assertThat(user).isEqualTo(USER);
    }

    @Test
    void testChunked() throws IOException {
        var instance = Users.INSTANCE;
        var writer = instance.chunkedChannelWriter(50);
        var baos = new ByteArrayOutputStream();
        try (
            var channel = Channels.newChannel(baos)
        ) {
            writer.write(USER, channel);
        }
        var userJson = baos.toString();
        System.out.println(userJson);
    }

    public static final User USER = new User(
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
                    "bar", false, UUID.randomUUID(), Map.of("foo", "bar")
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
}
