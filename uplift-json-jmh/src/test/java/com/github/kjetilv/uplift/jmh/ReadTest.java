package com.github.kjetilv.uplift.jmh;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.LineSegments;
import com.github.kjetilv.uplift.json.JsonReader;
import com.github.kjetilv.uplift.json.Token;
import com.github.kjetilv.uplift.json.TokenTrie;
import com.github.kjetilv.uplift.json.events.LineSegmentJsonReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadTest {

    @Test
    void read() {
//        Tweet read1 = bReader.read(data);
//        assertThat(read1).isNotNull();

        Tweet read2 = reader.read(lineSegment);
        assertThat(read2).isNotNull();

//        assertThat(read1).isEqualTo(read2);

        TokenTrie tokenTrie = Tweet_Callbacks.PRESETS.getTokenTrie();
        Token.Field resolve1 = tokenTrie.get("retweeters_count");
        Token.Field resolve2 = tokenTrie.get("retweeters_count");
        assertThat(resolve1).isNotNull().isSameAs(resolve2);
//
    }

    private static final URL RESOURCE = Objects.requireNonNull(
        Thread.currentThread().getContextClassLoader().getResource("48.json"), "resource");

    private static final byte[] data;

    private static final LineSegment lineSegment;

    private static final JsonReader<LineSegment, Tweet> reader;

    private static final JsonReader<byte[], Tweet> bReader;

    private static final int X = 2_000_000;

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
        lineSegment = LineSegments.of(data);
        System.out.println(Tweet_Callbacks.PRESETS);
        bReader = TweetRW.INSTANCE.bytesReader();
        reader = new LineSegmentJsonReader<>(TweetRW.INSTANCE.callbacks());
    }
}
