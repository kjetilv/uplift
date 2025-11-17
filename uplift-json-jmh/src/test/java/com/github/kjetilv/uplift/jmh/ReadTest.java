package com.github.kjetilv.uplift.jmh;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.JsonReader;
import com.github.kjetilv.uplift.json.mame.CachingJsonSessions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
public class ReadTest {

    public static final ObjectMapper objectMapper = new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .setDefaultPropertyInclusion(JsonInclude.Include.NON_DEFAULT);

    //    @Fork(value = 2, warmups = 2)
//    @Threads(8)
    @Benchmark
    public Tweet readTweetUplift() {
        var reader1 = TweetRW.INSTANCE.bytesReader();
        return reader1.read(data);
    }

    @Test
    void read() throws IOException {
        var read1 = bReader.read(data);
        var read1a = bReader.read(data);
        assertThat(read1).isNotNull();

        var read3 = objectMapper.readValue(data, Tweet.class);
        assertThat(read3).isNotNull();

//        assertThat(read2).isEqualTo(read3);
//        assertThat(read1).isEqualTo(read3);

        var tokenTrie = Tweet_Callbacks.PRESETS.getTokenTrie();
        var resolve1 = tokenTrie.get("retweeters_count");
        var resolve2 = tokenTrie.get("retweeters_count");
        assertThat(resolve1).isNotNull().isSameAs(resolve2);
//
    }

    private static final URL RESOURCE = Objects.requireNonNull(
        Thread.currentThread().getContextClassLoader().getResource("48.json"), "resource");

    private static final byte[] data;

    private static final JsonReader<byte[], Tweet> bReader;

    static {
        try (
            var out = new ByteArrayOutputStream()
        ) {
            try (var inputStream = RESOURCE.openStream()) {
                inputStream.transferTo(out);
            }
            data = out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bReader = TweetRW.INSTANCE.bytesReader(CachingJsonSessions.create(HashKind.K128));
    }
}
