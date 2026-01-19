package com.github.kjetilv.uplift.asynchttp;

import com.github.kjetilv.uplift.hash.HashKind;
import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorSpecies;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SyncTest {

    @Test
    void vectors() {
        var parsed = """
            foo
            bar
            zot
            
            
            
            sdfsdfsdfsdf
            2345""";
        var byteVector1 = ByteVector.fromArray(SPECIES, parsed.getBytes(), 0);
        var byteVector2 = ByteVector.fromArray(SPECIES, parsed.getBytes(), byteVector1.length());

        System.out.println(byteVector1.eq((byte) '\n'));
        System.out.println(byteVector2.eq((byte) '\n'));
    }

    @Test
    void test() {
        var server = SyncIOServer.create();

        var run = server.run((in, out) -> {
            try {
                var inBuffer = ByteBuffer.allocate(1024);
                var outBuffer = ByteBuffer.allocate(1024);
                var read = in.read(inBuffer);
                HttpBytes.read(inBuffer)
                    .ifPresent(httpBytes -> {
                        var req = HttpReq.readRequest(httpBytes);
                        var res = new HttpRes(
                            200,
                            "world\n".getBytes(),
                            HashKind.K128.random()
                        );
                        var headerBytes = res.toResponseHeader().getBytes(UTF_8);
                        BufferingWriter<ByteBuffer> writer = new SyncByteChannelBufferingWriter(out);
                        writer.write(new WritableBuffer<>(ByteBuffer.wrap(headerBytes), headerBytes.length));
                        if (res.hasBody()) {
                            writer.write(new WritableBuffer<>(ByteBuffer.wrap(res.body()), res.body().length));
                        }
                    });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(run.port());

        CompletableFuture<HttpResponse<String>> future;
        try (var httpClient = HttpClient.newHttpClient()) {
            future = httpClient.sendAsync(
                HttpRequest.newBuilder(URI.create("http://localhost:" + run.port())).build(),
                HttpResponse.BodyHandlers.ofString()
            );
        }

        var body = future.join().body();

        System.out.println(body);

        server.close();
        try {
            run.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testHttp() {
        var handler = new HttpSyncHandler((_, _) ->
            new com.github.kjetilv.uplift.asynchttp.rere.HttpResponse(
                200,
                "world"
            ));
        var server = SyncIOServer.create();
        var run = server.run(handler);

        CompletableFuture<HttpResponse<String>> future;
        try (var httpClient = HttpClient.newHttpClient()) {
            future =
                httpClient.sendAsync(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + run.port())).build(),
                    HttpResponse.BodyHandlers.ofString()
                );
        }
        assertThat(future)
            .isCompletedWithValueMatching(response ->
                response.body().equals("world"));

        server.close();
        run.join();
    }

    static final VectorSpecies<Byte> SPECIES = ByteVector.SPECIES_PREFERRED;
}
