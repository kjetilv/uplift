package com.github.kjetilv.uplift.synchttp;

import module java.base;
import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorSpecies;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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

        var mask1 = byteVector1.eq((byte) '\n');
        var mark2 = byteVector2.eq((byte) '\n');

        assertThat(mask1).hasToString("Mask[...T...T...TTTT.]");
        assertThat(mark2).hasToString("Mask[...........T....]");
    }

    @Test
    void testHttpCallbacks() {
        var handler = new HttpCallbackProcessor((_, callbacks) ->
            callbacks.status(200).contentLength(5).body("world"));
        var server = Server.create().run(handler);

        CompletableFuture<HttpResponse<String>> future;
        try (var httpClient = HttpClient.newHttpClient()) {
            future =
                httpClient.sendAsync(
                    HttpRequest.newBuilder(server.uri()).build(),
                    HttpResponse.BodyHandlers.ofString()
                );
        }
        assertThat(future)
            .isCompletedWithValueMatching(response ->
                response.body().equals("world"));

        server.close();
        server.join();
    }

    static final VectorSpecies<Byte> SPECIES = ByteVector.SPECIES_PREFERRED;
}
