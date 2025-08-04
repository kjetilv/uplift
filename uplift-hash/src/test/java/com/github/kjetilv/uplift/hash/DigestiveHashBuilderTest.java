package com.github.kjetilv.uplift.hash;

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
        HashBuilder<Bytes, K128> builder = Hashes.hashBuilder(K128);

        builder.hash(Bytes.from("foo".getBytes(UTF_8)));
        builder.hash(Bytes.from("bar".getBytes(StandardCharsets.UTF_8)));
        Hash<K128> hash1 = builder.build();

        builder.hash(Bytes.from("zot".getBytes(StandardCharsets.UTF_8)));
        Hash<K128> hash2 = builder.build();

        builder.hash(Bytes.from("foo".getBytes(StandardCharsets.UTF_8)));
        builder.hash(Bytes.from("bar".getBytes(StandardCharsets.UTF_8)));
        Hash<K128> hash3 = builder.build();

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

        HashBuilder<String, K128> map = Hashes.hashBuilder(K128)
            .map(ssb);

        HashBuilder<String, K128> flatMap = Hashes.hashBuilder(K128)
            .map(ssb).flatMap(sss);

        Hash<K128> mapHash = map.hash("foo").build();
        Hash<K128> flatMapHash = flatMap.hash("foo").build();

        assertEquals(mapHash, flatMapHash);
    }

}