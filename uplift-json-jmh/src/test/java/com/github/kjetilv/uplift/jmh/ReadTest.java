package com.github.kjetilv.uplift.jmh;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kjetilv.uplift.json.JsonReader;
import com.github.kjetilv.uplift.json.Token;
import com.github.kjetilv.uplift.json.TokenResolver;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadTest {

    public static final ObjectMapper objectMapper = new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .setDefaultPropertyInclusion(JsonInclude.Include.NON_DEFAULT)
        .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);

    //    @Fork(value = 2, warmups = 2)
//    @Threads(8)
    @Benchmark
    public Tweet readTweetUplift() {
        JsonReader<byte[], Tweet> reader1 = TweetRW.INSTANCE.bytesReader();
        return reader1.read(data);
    }

    @Test
    void read() throws IOException {
        Tweet read1 = bReader.read(data);
        assertThat(read1).isNotNull();

        Tweet read3 = objectMapper.readValue(data, Tweet.class);
        assertThat(read3).isNotNull();

//        assertThat(read2).isEqualTo(read3);
//        assertThat(read1).isEqualTo(read3);

        TokenResolver tokenTrie = Tweet_Callbacks.PRESETS.getTokenTrie();
        Token.Field resolve1 = tokenTrie.get("retweeters_count");
        Token.Field resolve2 = tokenTrie.get("retweeters_count");
        assertThat(resolve1).isNotNull().isSameAs(resolve2);
//
    }

    private static final URL RESOURCE = Objects.requireNonNull(
        Thread.currentThread().getContextClassLoader().getResource("48.json"), "resource");

    private static final byte[] data;

    private static final JsonReader<byte[], Tweet> bReader;

    static {
        try (
            ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            try (InputStream inputStream = RESOURCE.openStream()) {
                inputStream.transferTo(out);
            }
            data = out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(Tweet_Callbacks.PRESETS);
        bReader = TweetRW.INSTANCE.bytesReader();
    }
}
