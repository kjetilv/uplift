package com.github.kjetilv.uplift.hash;

import com.github.kjetilv.uplift.util.Bytes;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DigestiveHashBuilderTest {

    @Test
    void test() {
        var builder = HashBuilder.forKind(K128);

        builder.hash(Bytes.from("foo".getBytes(UTF_8)));
        builder.hash(Bytes.from("bar".getBytes(StandardCharsets.UTF_8)));
        var hash1 = builder.build();

        builder.hash(Bytes.from("zot".getBytes(StandardCharsets.UTF_8)));
        var hash2 = builder.build();

        builder.hash(Bytes.from("foo".getBytes(StandardCharsets.UTF_8)));
        builder.hash(Bytes.from("bar".getBytes(StandardCharsets.UTF_8)));
        var hash3 = builder.build();

        assertNotEquals(hash1, hash2);
        assertEquals(hash1, hash3);
    }

    @Test
    void testMap() {
        Function<String, Stream<String>> sss = s ->
            s.chars()
                .mapToObj(c ->
                    (char) c)
                .map(String::valueOf);

        Function<String, Bytes> ssb = s ->
            Bytes.from(s.getBytes(UTF_8));

        var map = HashBuilder.forKind(K128)
            .map(ssb);

        var flatMap = HashBuilder.forKind(K128)
            .map(ssb).flatMap(sss);

        var mapHash = map.hash("foo").build();
        var flatMapHash = flatMap.hash("foo").build();

        assertEquals(mapHash, flatMapHash);
    }

}