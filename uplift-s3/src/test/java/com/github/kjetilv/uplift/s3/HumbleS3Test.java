package com.github.kjetilv.uplift.s3;

import com.github.kjetilv.uplift.kernel.Env;
import com.github.kjetilv.uplift.kernel.io.BytesIO;
import com.github.kjetilv.uplift.kernel.io.Range;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({ "UseOfSystemOutOrSystemErr", "MagicNumber" })
class HumbleS3Test {

    @Test
    @Disabled
    void getIds() {
        S3Accessor defaultS3Accessor = new DefaultS3Accessor(
            Env.actual(), HttpClient.newHttpClient(), "taninim-water", null);

        //        humbleS3Accessor.stream("ids.json").map(BytesIO::readUTF8).ifPresent(System.out::println);
        System.out.println(defaultS3Accessor.remoteInfos("abc"));

        String contents = UUID.randomUUID().toString();
        System.out.println("Putting: " + contents);

        defaultS3Accessor.put(contents, "foobar.txt");

        defaultS3Accessor.stream("foobar.txt")
            .map(BytesIO::readUTF8)
            .ifPresent(x ->
                System.out.println("Retrieved: " + x));

        defaultS3Accessor.stream("foobar.txt", new Range(2L, 10L, 9L))
            .map(BytesIO::readUTF8)
            .ifPresent(x ->
                System.out.println("Retrieved: " + x));

        defaultS3Accessor.remove(
            List.of("foobar.txt", "ExampleObject.txt")
        );
    }
}
