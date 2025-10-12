package com.github.kjetilv.uplift.s3;

import com.github.kjetilv.uplift.kernel.Env;
import com.github.kjetilv.uplift.kernel.io.BytesIO;
import com.github.kjetilv.uplift.kernel.io.Range;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"UseOfSystemOutOrSystemErr", "MagicNumber"})
class HumbleS3Test {

    @Disabled
    @Test
    void getIds() {
        S3Accessor defaultS3Accessor = new DefaultS3Accessor(
            Env.actual(), HttpClient.newHttpClient(), "taninim-water", null);

        //        humbleS3Accessor.stream("ids.json").map(BytesIO::readUTF8).ifPresent(System.out::println);
        var abc = defaultS3Accessor.remoteInfos("abc");
        assertThat(abc).isNotEmpty().allSatisfy((s, remoteInfo) ->
            assertThat(s).startsWith("abc"));
        System.out.println(abc);

        var contents = UUID.randomUUID().toString();
        defaultS3Accessor.put(contents, "foobar.txt");

        var readFoobar = defaultS3Accessor.stream("foobar.txt")
            .map(BytesIO::readUTF8);
        assertThat(readFoobar).hasValue(contents);

        var rangedFoobar = defaultS3Accessor
            .stream("foobar.txt", new Range(2L, 10L, 9L))
            .map(BytesIO::readUTF8);
        assertThat(rangedFoobar)
            .hasValue(contents.substring(2, 10));

        defaultS3Accessor.remove(
            List.of("foobar.txt", "ExampleObject.txt")
        );
    }
}
